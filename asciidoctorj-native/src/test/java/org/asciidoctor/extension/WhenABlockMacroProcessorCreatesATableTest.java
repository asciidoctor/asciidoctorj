package org.asciidoctor.extension;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.test.extension.ClasspathHelper;
import org.asciidoctor.util.TestHttpServer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WhenABlockMacroProcessorCreatesATableTest {

    public static final String FIRST_TD = "td:first-child";
    public static final String SECOND_TD = "td:nth-child(2)";
    public static final String THIRD_TD = "td:nth-child(3)";
    public static final String IMG_ELEMENT = "img";
    public static final String COL = "col";
    public static final String STYLE = "style";
    public static final String WIDTH = "width";
    public static final String WIDTH_2 = "2%";
    public static final String WIDTH_3 = "3%";
    public static final String WIDTH_20 = "20%";
    public static final String WIDTH_40 = "40%";
    public static final String CONTRIBUTOR = "bob";
    public static final String BLOCKMACRO_NAME = "githubcontributors";

    public static final String AVATAR_URL_REGEXP = "https://avatars.githubusercontent.com/u/.*";
    public static final String CSS_QUERY_TABLE = "table";
    public static final String CLASS_GRID_ROWS = "grid-rows";
    public static final String CLASS_HALIGN_LEFT = "halign-left";
    public static final String CLASS_HALIGN_CENTER = "halign-center";
    public static final String ATTR_SRC = "src";
    public static final int THREE = 3;

    private Asciidoctor asciidoctor;

    @TempDir
    private File tempDir;

    private static final String DOCUMENT = """

            = AsciidoctorJRuby contributors

            githubcontributors::asciidoctor/asciidoctorj[]
            """;

    private static final String DOCUMENT_WITH_NEGATIVE_WIDTHS = """

            = AsciidoctorJRuby contributors

            githubcontributors::asciidoctor/asciidoctorj[widths="2,3,-1"]
            """;

    private ClasspathHelper classpathResources;

    @BeforeEach
    public void setup() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper(WhenABlockMacroProcessorCreatesATableTest.class);
        TestHttpServer.start(Map.of("http://api.github.com/repos/asciidoctor/asciidoctorj/contributors", classpathResources.getResource("githubcontributors.json")));
    }

    @AfterEach
    public void cleanup() {
        TestHttpServer.getInstance().stop();
    }

    // NOTE like the =~ operator in Groovy, the regular expression matches a part of the value
    private static boolean matchesAvatarUrl(String value) {
        return Pattern.compile(AVATAR_URL_REGEXP).matcher(value).find();
    }

    private static void assertHtmlTable(Document htmlDocument) {
        assertThat(htmlDocument.select(CSS_QUERY_TABLE).hasClass(CLASS_GRID_ROWS)).isTrue();

        assertThat(htmlDocument.select(FIRST_TD).stream().allMatch(tdElement -> tdElement.select(IMG_ELEMENT).size() == 1)).isTrue();
        assertThat(htmlDocument.select(FIRST_TD).stream().allMatch(tdElement -> matchesAvatarUrl(tdElement.select(IMG_ELEMENT).get(0).attr(ATTR_SRC)))).isTrue();

        // NOTE the Groovy assertion htmlDocument.select(SECOND_TD).size() == htmlDocument.select(SECOND_TD) != 0 was always true
        assertThat(htmlDocument.select(SECOND_TD).size()).isNotEqualTo(0);

        assertThat(htmlDocument.select(SECOND_TD).stream().allMatch(tdElement -> tdElement.hasClass(CLASS_HALIGN_LEFT))).isTrue();
        assertThat(htmlDocument.select(THIRD_TD).stream().allMatch(tdElement -> tdElement.hasClass(CLASS_HALIGN_CENTER))).isTrue();

        assertThat(htmlDocument.select(SECOND_TD).stream().anyMatch(tdElement -> tdElement.text().equals(CONTRIBUTOR))).isTrue();
    }

    @Test
    @DisplayName("the table should be rendered to html")
    public void theTableShouldBeRenderedToHtml() throws Exception {
        // given:
        asciidoctor.javaExtensionRegistry().blockMacro(BLOCKMACRO_NAME, GithubContributorsBlockMacro.class);
        File resultFile = new File(tempDir, "result.html");

        // when:
        Options options = Options.builder()
                .safe(SafeMode.SAFE)
                .inPlace(false)
                .baseDir(tempDir)
                .toDir(tempDir)
                .toFile(resultFile)
                .build();
        asciidoctor.convert(DOCUMENT, options);

        // then:
        Document htmlDocument = Jsoup.parse(resultFile, UTF_8.name());

        Elements cols = htmlDocument.select(COL);
        assertThat(cols.size()).isEqualTo(THREE);
        assertThat(cols.get(0).attr(STYLE).contains(WIDTH_20) || cols.get(0).attr(WIDTH).equals(WIDTH_20)).isTrue();
        assertThat(cols.get(1).attr(STYLE).contains(WIDTH_40) || cols.get(1).attr(WIDTH).equals(WIDTH_40)).isTrue();
        assertThat(cols.get(2).attr(STYLE).contains(WIDTH_40) || cols.get(2).attr(WIDTH).equals(WIDTH_40)).isTrue();

        assertHtmlTable(htmlDocument);
    }

    @Test
    @DisplayName("the table should be rendered to html with negative widths")
    public void theTableShouldBeRenderedToHtmlWithNegativeWidths() throws Exception {
        // given:
        asciidoctor.javaExtensionRegistry().blockMacro(BLOCKMACRO_NAME, GithubContributorsBlockMacro.class);
        File resultFile = new File(tempDir, "resultWithNegativeWidth.html");

        // when:
        Options options = Options.builder()
                .safe(SafeMode.SAFE)
                .inPlace(false)
                .baseDir(tempDir)
                .toDir(tempDir)
                .toFile(resultFile)
                .build();
        asciidoctor.convert(DOCUMENT_WITH_NEGATIVE_WIDTHS, options);

        // then:
        Document htmlDocument = Jsoup.parse(resultFile, UTF_8.name());

        Elements cols = htmlDocument.select(COL);
        assertThat(cols.size()).isEqualTo(THREE);
        assertThat(cols.get(0).attr(STYLE).contains(WIDTH_2) || cols.get(0).attr(WIDTH).equals(WIDTH_2)).isTrue();
        assertThat(cols.get(1).attr(STYLE).contains(WIDTH_3) || cols.get(1).attr(WIDTH).equals(WIDTH_3)).isTrue();
        assertThat(cols.get(2).attr(STYLE).length() == 0 && cols.get(2).attr(WIDTH).length() == 0).isTrue();

        assertHtmlTable(htmlDocument);
    }

    @Test
    @DisplayName("the table should be rendered to docbook")
    public void theTableShouldBeRenderedToDocbook() throws Exception {
        // given:
        asciidoctor.javaExtensionRegistry().blockMacro(BLOCKMACRO_NAME, GithubContributorsBlockMacro.class);
        File resultFile = new File(tempDir, "result.db");

        // when:
        Options options = Options.builder()
                .backend("docbook5")
                .safe(SafeMode.SAFE)
                .inPlace(false)
                .baseDir(tempDir)
                .toDir(tempDir)
                .toFile(resultFile)
                .build();
        asciidoctor.convert(DOCUMENT, options);

        // then:
        // NOTE the XML was read with XmlSlurper in Groovy (which ignores the namespaces)
        org.w3c.dom.Document rootNode = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resultFile);
        NodeList rowNodes = (NodeList) XPathFactory.newInstance().newXPath()
                .evaluate("/*/*[local-name()='table']/*[local-name()='tgroup']/*[local-name()='tbody']/*", rootNode, XPathConstants.NODESET);
        List<List<Element>> rows = new ArrayList<>();
        for (int i = 0; i < rowNodes.getLength(); i++) {
            List<Element> entries = new ArrayList<>();
            NodeList children = rowNodes.item(i).getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                if (children.item(j) instanceof Element element && element.getTagName().equals("entry")) {
                    entries.add(element);
                }
            }
            rows.add(entries);
        }
        assertThat(rows.stream().anyMatch(row -> row.get(1).getTextContent().equals(CONTRIBUTOR))).isTrue();
        assertThat(rows.size()).isGreaterThan(0);
        assertThat(rows.stream().allMatch(row -> {
            Element imagedata = (Element) row.get(0).getElementsByTagName("imagedata").item(0);
            return imagedata != null && matchesAvatarUrl(imagedata.getAttribute("fileref"));
        })).isTrue();
        assertThat(rows.stream().allMatch(row -> row.get(1).getAttribute("align").equals("left"))).isTrue();
        assertThat(rows.stream().allMatch(row -> row.get(2).getAttribute("align").equals("center"))).isTrue();
    }

}
