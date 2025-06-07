package org.asciidoctor;

import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.junit.Assert.assertEquals;
import static org.xmlmatchers.xpath.HasXPath.hasXPath;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenAttributesAreUsedInAsciidoctor {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("documentwithnote.asciidoc")
    private File documentWithNote;

    @ClasspathResource("tocsample.asciidoc")
    private File tocSample;

    @ClasspathResource("toc2sample.asciidoc")
    private File toc2Sample;

    @ClasspathResource("rendersample.asciidoc")
    private File renderSample;

    @TempDir
    private File tempFolder;


    @Test
    public void qualified_http_url_inline_with_hide_uri_scheme_set() {

        Attributes attributes = Attributes.builder()
                .hiddenUriScheme(true)
                .build();

        String content = asciidoctor.convert("The AsciiDoc project is located at https://asciidoc.org.", Options.builder().attributes(attributes).build());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertThat(link.text(), is("asciidoc.org"));

    }

    @Test
    public void compat_mode_should_change_how_document_is_rendered_to_legacy_system() {

        Attributes attributes = Attributes.builder()
                .attribute("version", "1.0.0")
                .compatMode(CompatMode.LEGACY)
                .build();

        String content = asciidoctor.convert("The `AsciiDoc {version}` project.", Options.builder().attributes(attributes).build());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element code = doc.getElementsByTag("code").first();
        assertThat(code.text(), containsString("{version}"));

    }

    @Test
    public void no_compat_mode_should_change_how_document_is_rendered_to_new_system() {

        Attributes attributes = Attributes.builder()
                .attribute("version", "1.0.0")
                .build();

        String content = asciidoctor.convert("The `AsciiDoc {version}` project.", Options.builder().attributes(attributes).build());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element code = doc.getElementsByTag("code").first();
        assertThat(code.text(), containsString("1.0.0"));

    }

    @Test
    public void should_preload_open_cache_uri_gem() {

        Attributes attributes = Attributes.builder()
                .cacheUri(true)
                .build();

        String content = asciidoctor.convert("read my lips", Options.builder().attributes(attributes).build());

        assertThat(content, is(notNullValue()));

    }

    @Test
    public void should_add_AsciiMath_delimiters_around_math_block_content_if_math_attribute_not_latexmath(
            @ClasspathResource("math.asciidoc") File sourceDocument) throws IOException {

        Attributes attributes = Attributes.builder().math("asciimath").build();

        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(sourceDocument, options);

        Document doc = Jsoup.parse(new File(tempFolder, "math.html"), "UTF-8");

        assertThat(doc.getElementsByAttributeValue("type", "text/x-mathjax-config").size(), is(1));
    }

    @Test
    public void should_use_custom_appendix_caption_if_specified(
            @ClasspathResource("appendix.asciidoc") File appendixDocument) throws IOException {

        Attributes attributes = Attributes.builder().appendixCaption("App").build();

        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(appendixDocument, options);

        Document doc = Jsoup.parse(new File(tempFolder,
                "appendix.html"), "UTF-8");

        Element attributeElement = doc.getElementById("_attribute_options");
        assertThat(attributeElement.text(), startsWith("App"));
    }

    @Test
    public void should_add_a_hardbreak_at_end_of_each_line_when_hardbreaks_option_is_set() {

        Attributes attributes = Attributes.builder().hardbreaks(true).build();

        String content = asciidoctor.convert("read\nmy\nlips", Options.builder().attributes(attributes).build());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element paragraph = doc.getElementsByAttributeValue("class", "paragraph").first();
        assertThat(paragraph.getElementsByTag("br").size(), is(2));

    }

    @Test
    public void sect_num_levels_attribute_should_only_number_levels_up_to_value_defined_by_sectnumlevels_attribute(
            @ClasspathResource("multiple_levels.asciidoc") File multiLevelDocument) throws IOException {

        Attributes attributes = Attributes.builder().sectionNumbers(true).sectNumLevels(2).build();

        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder).attributes(attributes)
                .build();

        asciidoctor.convertFile(multiLevelDocument, options);

        Document doc = Jsoup.parse(new File(tempFolder, "multiple_levels.html"), "UTF-8");

        assertThat(doc.getElementById("_level_1").text(), startsWith("1."));
        assertThat(doc.getElementById("_level_2").text(), startsWith("1.1."));
        assertThat(doc.getElementById("_level_3").text(), not(startsWith("1.1.1.")));
        assertThat(doc.getElementById("_level_4").text(), not(startsWith("1.1.1.1.")));
    }

    @Test
    public void no_footer_attribute_should_not_show_footer_info() throws IOException {

        Attributes attributes = Attributes.builder().noFooter(true).build();

        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "rendersample.html"), "UTF-8");

        assertThat(doc.getElementById("footer"), is(nullValue()));

    }


    @Test
    public void show_title_true_attribute_should_show_title_on_embedded_document() {
        final Options options = Options.builder()
                .attributes(Attributes.builder().showTitle(true).build())
                .toFile(false)
                .standalone(false)
                .build();

        final Document doc = Jsoup.parse(asciidoctor.convertFile(renderSample, options));

        assertEquals(1, doc.getElementsByTag("h1").size());
        assertEquals("Document Title", doc.getElementsByTag("h1").get(0).text());
    }

    @Test
    public void show_title_false_then_true_attribute_should_show_title_on_embedded_document() {
        final Options options = Options.builder()
                .attributes(Attributes.builder().showTitle(false).showTitle(true).build())
                .toFile(false)
                .standalone(false)
                .build();

        final Document doc = Jsoup.parse(asciidoctor.convertFile(renderSample, options));

        assertEquals(1, doc.getElementsByTag("h1").size());
        assertEquals("Document Title", doc.getElementsByTag("h1").get(0).text());
    }

    @Test
    public void show_title_false_attribute_should_hide_title_on_embedded_document() {
        final Options options = Options.builder()
                .attributes(Attributes.builder().showTitle(false).build())
                .toFile(false)
                .standalone(false)
                .build();

        final Document doc = Jsoup.parse(asciidoctor.convertFile(renderSample, options));

        assertEquals(0, doc.getElementsByTag("h1").size());
    }

    @Test
    public void show_title_true_then_false_attribute_should_hide_title_on_embedded_document() {
        final Options options = Options.builder()
                .attributes(Attributes.builder().showTitle(true).showTitle(false).build())
                .toFile(false)
                .standalone(false)
                .build();

        final Document doc = Jsoup.parse(asciidoctor.convertFile(renderSample, options));

        assertEquals(0, doc.getElementsByTag("h1").size());
    }

    @Test
    public void show_title_true_attribute_should_show_title_on_standalone_document() {

        final Options options = Options.builder()
                .attributes(Attributes.builder().showTitle(true).build())
                .toFile(false)
                .standalone(true)
                .build();

        final Document doc = Jsoup.parse(asciidoctor.convertFile(renderSample, options));

        assertEquals(1, doc.getElementsByTag("h1").size());
        assertEquals("Document Title", doc.getElementsByTag("h1").get(0).text());
    }

    @Test
    public void show_title_false_attribute_should_hide_title_on_standalone_document() {

        final Options options = Options.builder()
                .attributes(Attributes.builder().showTitle(false).build())
                .toFile(false)
                .standalone(true)
                .build();

        final Document doc = Jsoup.parse(asciidoctor.convertFile(renderSample, options));

        assertEquals(0, doc.getElementsByTag("h1").size());
    }

    @Test
    public void show_title_true_then_false_attribute_should_hide_title_on_standalone_document() {

        final Options options = Options.builder()
                .attributes(Attributes.builder().showTitle(false).build())
                .toFile(false)
                .standalone(true)
                .build();

        final Document doc = Jsoup.parse(asciidoctor.convertFile(renderSample, options));

        assertEquals(0, doc.getElementsByTag("h1").size());
    }


    @Test
    public void source_highlighter_attribute_should_add_required_javascript_libraries_as_highlighter() throws IOException {

        Attributes attributes = Attributes.builder().sourceHighlighter("prettify").build();

        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "rendersample.html"), "UTF-8");
        Elements link = doc.select("link");
        assertThat(link.get(1).attr("href"), containsString("prettify.min.css"));

    }

    @Test
    public void render_content_without_attributes_should_embed_css_by_default() throws IOException {

        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .build();

        asciidoctor.convertFile(renderSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "rendersample.html"), "UTF-8");
        Elements cssStyle = doc.select("style");
        assertThat(cssStyle.html(), is(not("")));

        Elements link = doc.select("link");
        assertThat(link.html(), is("".trim()));

    }

    @Test
    public void should_skip_front_matter_if_specified_by_skip_front_matter_attribute(
            @ClasspathResource("renderwithfrontmatter.adoc") File renderWithFrontMatter) {

        Attributes attributes = Attributes.builder().skipFrontMatter(true).build();
        Options options = Options.builder().toFile(false).inPlace(false).attributes(attributes).build();

        String content = asciidoctor.convertFile(renderWithFrontMatter, options);
        Document doc = Jsoup.parse(content, "UTF-8");
        Elements hrElements = doc.getElementsByTag("hr");

        assertThat(hrElements.size(), is(0));

    }

    @Test
    public void set_anchors_attribute_should_add_anchor_to_sections() {

        Attributes attributes = Attributes.builder().setAnchors(true).build();
        Options options = Options.builder().inPlace(false).toFile(false).attributes(attributes).build();

        String content = asciidoctor.convertFile(renderSample, options);

        Document doc = Jsoup.parse(content, "UTF-8");
        Element anchorElement = doc.select("a[class=anchor]").first();

        assertThat(anchorElement.attr("href"), is("#_section_a"));

    }

    @Test
    public void ignore_undefined_attributes_should_keep_lines_with_undefined_attributes(
            @ClasspathResource("documentwithundefinedattribute.asciidoc") File sourceDocument) {

        Attributes attributes = Attributes.builder().ignoreUndefinedAttributes(true).build();
        Options options = Options.builder().toFile(false).attributes(attributes).build();

        String renderContent = asciidoctor.convertFile(
                sourceDocument,
                options);
        assertThat(renderContent, containsString("{bogus-attribute}"));
    }

    @Test
    public void setting_toc_attribute_table_of_contents_should_be_generated() throws IOException {

        Attributes attributes = Attributes.builder().tableOfContents(true).build();
        Options options = Options.builder()
                .inPlace(false)
                .toDir(tempFolder)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(tocSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "tocsample.html"), "UTF-8");
        Elements tocElement = doc.select("div.toc");
        assertThat(tocElement.hasClass("toc"), is(true));

    }

    @Test
    public void setting_toc_attribute_left_should_work() throws IOException {
        Attributes attributes = Attributes.builder().tableOfContents(Placement.LEFT).build();
        Options options = Options.builder().inPlace(false).toDir(tempFolder).safe(SafeMode.UNSAFE).attributes(attributes).build();

        asciidoctor.convertFile(tocSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "tocsample.html"), "UTF-8");
        Elements tocElement = doc.select("body.toc-left > div#header > div#toc");
        assertThat(tocElement.size(), is(1));
    }

    @Test
    public void setting_toc_attribute_right_should_work() throws IOException {
        Attributes attributes = Attributes.builder().tableOfContents(Placement.RIGHT).build();
        Options options = Options.builder()
                .inPlace(false)
                .toDir(tempFolder)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(tocSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "tocsample.html"), "UTF-8");
        Elements tocElement = doc.select("body.toc-right > div#header > div#toc");
        assertThat(tocElement.size(), is(1));
    }

    @Test
    public void setting_toc_attribute_preamble_should_work() throws IOException {
        Attributes attributes = Attributes.builder().tableOfContents(Placement.PREAMBLE).build();
        Options options = Options.builder()
                .inPlace(false)
                .toDir(tempFolder)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(tocSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "tocsample.html"), "UTF-8");
        Elements tocElement = doc.select("body.article > div#content > div#preamble > div#toc");
        assertThat(tocElement.size(), is(1));
    }

    @Test
    public void setting_toc_attribute_macro_should_work(
            @ClasspathResource("tocsamplemacro.asciidoc") File tocSampleMacro) throws IOException {

        Attributes attributes = Attributes.builder().tableOfContents(Placement.MACRO).build();
        Options options = Options.builder()
                .inPlace(false)
                .toDir(tempFolder)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(tocSampleMacro, options);

        Document doc = Jsoup.parse(new File(tempFolder, "tocsamplemacro.html"), "UTF-8");
        Elements tocElement = doc.select("body.article > div#content > div.sect1 > div.sectionbody > div#toc");
        assertThat(tocElement.size(), is(1));
    }

    @Test
    public void attribute_missing_should_drop_line_should_drop_line_with_reference_to_missing_attribute_if_attribute_missing_attribute_is_drop_line() {

        Attributes attributes = Attributes.builder().attributeMissing("drop-line").build();
        Options options = Options.builder().attributes(attributes).build();

        String renderContent = asciidoctor.convert("This is\n"
                + "blah blah {foobarbaz}\n" + "all there is.", options);

        assertThat(renderContent, not(containsString("{foobarbaz}")));

    }

    @Test
    public void attribute_undefined_should_not_drop_line_with_attribute_unassignment_if_attribute_undefined_is_drop() {

        Attributes attributes = Attributes.builder().attributeUndefined("drop").build();
        Options options = Options.builder().attributes(attributes).build();

        String renderContent = asciidoctor.convert(":foo:\n\n{set:foo!}\n{foo}yes", options);

        Document doc = Jsoup.parse(renderContent, "UTF-8");
        Element paragraph = doc.getElementsByTag("p").first();
        assertThat(paragraph.text(), is("{foo}yes"));
    }

    @Test
    public void table_of_content_should_be_placeable() throws IOException {

        Attributes attributes = Attributes.builder().tableOfContents(Placement.RIGHT).build();
        Options options = Options.builder()
                .inPlace(false)
                .toFile(new File(tempFolder, "toc2sample.html"))
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(toc2Sample, options);

        File renderedFile = new File(tempFolder, "toc2sample.html");
        Document doc = Jsoup.parse(renderedFile, "UTF-8");
        Elements body = doc.select("body");
        String classAttribute = body.attr("class");
        String[] classAttributes = classAttribute.split(" ");
        assertThat(classAttributes, hasItemInArray("toc2"));
        assertThat(classAttributes, hasItemInArray("toc-right"));

        renderedFile.delete();
    }

    @Test
    public void setting_linkcss_as_false_in_string_should_embed_css_file() throws IOException {

        Attributes attributes = Attributes.builder().attribute("linkcss!").build();
        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        // String readFull = IOUtils.readFull(new FileInputStream(new
        // File(testFolder.getRoot(), "rendersample.html")));

        Document doc = Jsoup.parse(new File(tempFolder, "rendersample.html"), "UTF-8");
        Elements cssStyle = doc.select("style");
        assertThat(cssStyle.html(), is(not("")));

        Elements link = doc.select("link");
        assertThat(link.html(), is("".trim()));

    }

    @Test
    public void setting_toc_attribute_and_numbered_in_string_form_table_of_contents_should_be_generated() throws IOException {

        Attributes attributes = Attributes.builder().arguments("toc sectnums").build();
        Options options = Options.builder()
                .inPlace(false)
                .toDir(tempFolder)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(tocSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "tocsample.html"), "UTF-8");
        Elements tocElement = doc.select("div.toc");
        assertThat(tocElement.hasClass("toc"), is(true));

        Element tocParagraph = doc.select("a[href=#_first_chapter]").first();
        assertThat(tocParagraph.text(), startsWith("1."));
    }

    @Test
    public void setting_toc_attribute_and_numbered_in_array_form_table_of_contents_should_be_generated() throws IOException {

        Attributes attributes = Attributes.builder()
                .attribute("toc")
                .attribute("sectnums")
                .build();

        Options options = Options.builder()
                .inPlace(false)
                .toDir(tempFolder)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(tocSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "tocsample.html"), "UTF-8");
        Elements tocElement = doc.select("div.toc");
        assertThat(tocElement.hasClass("toc"), is(true));

        Element tocParagraph = doc.select("a[href=#_first_chapter]").first();
        assertThat(tocParagraph.text(), startsWith("1."));
    }

    @Test
    public void unsetting_toc_attribute_table_of_contents_should_not_be_generated() {

        Attributes attributes = Attributes.builder().tableOfContents(false).build();
        Options options = Options.builder().toFile(false).attributes(attributes).build();

        String renderContent = asciidoctor.convertFile(tocSample, options);

        Document doc = Jsoup.parse(renderContent, "UTF-8");
        Elements tocElement = doc.select("div.toc");
        assertThat(tocElement.hasClass("toc"), is(false));

    }

    @Test
    public void styleSheetName_is_set_custom_stylesheet_should_be_used_()
            throws IOException {

        Attributes attributes = Attributes.builder().linkCss(true)
                .styleSheetName("mycustom.css").build();
        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "rendersample.html"), "UTF-8");
        Elements link = doc.select("link[href]");
        String attr = link.attr("href");
        assertThat(attr, is("./mycustom.css"));

    }

    @Test
    public void unsetting_styleSheetName_should_leave_document_without_style()
            throws IOException {

        Attributes attributes = Attributes.builder().unsetStyleSheet().build();
        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "rendersample.html"), "UTF-8");
        Elements link = doc.select("link[href]");
        String attr = link.attr("href");
        assertThat(attr, is(""));

    }

    @Test
    public void styles_dir_is_set_css_routes_should_use_it() throws IOException {

        Attributes attributes = Attributes.builder().stylesDir("./styles")
                .linkCss(true).styleSheetName("mycustom.css").build();
        Options options = Options.builder().inPlace(false).safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .mkDirs(true)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "rendersample.html"), "UTF-8");
        Elements link = doc.select("link[href]");
        String attr = link.attr("href");
        assertThat(attr, is("./styles/mycustom.css"));

    }

    @Test
    public void unsetting_linkcss_should_embed_css_file() throws IOException {

        Attributes attributes = Attributes.builder().linkCss(false).build();
        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        // String readFull = IOUtils.readFull(new FileInputStream(new
        // File(testFolder.getRoot(), "rendersample.html")));

        Document doc = Jsoup.parse(new File(tempFolder, "rendersample.html"), "UTF-8");
        Elements cssStyle = doc.select("style");
        assertThat(cssStyle.html(), is(not("")));

        Elements link = doc.select("link");
        assertThat(link.html(), is("".trim()));

    }

    @Test
    public void linkcss_should_not_embed_css_file() throws IOException {

        Attributes attributes = Attributes.builder().linkCss(true).build();
        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        Document doc = Jsoup.parse(new File(tempFolder, "rendersample.html"), "UTF-8");
        Elements link = doc.select("link[href]");
        String attr = link.get(1).attr("href");
        assertThat(attr, is("./asciidoctor.css"));

    }

    @Test
    public void copycss_with_in_place_should_copy_css_to_rendered_directory() {
        Attributes attributes = Attributes.builder().linkCss(true).copyCss(true).build();
        Options options = Options.builder()
                .inPlace(true)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        File cssFile = new File(renderSample.getParent(), "asciidoctor.css");
        assertThat(cssFile.exists(), is(true));
        cssFile.delete();

    }

    @Test
    public void copycss_negated_with_in_place_should_not_copy_css_to_rendered_directory() {
        Attributes attributes = Attributes.builder().copyCss(false).build();
        Options options = Options.builder()
                .inPlace(true)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        try {
            File cssFile = new File(renderSample.getParent(), "asciidoctor.css");
            assertThat(cssFile.exists(), is(false));
        } catch (RuntimeException e) {
            assertThat(e.getCause(), is(instanceOf(FileNotFoundException.class)));
        }

    }

    @Test
    public void copycss_and_linkcss_negated_should_not_copy_css_to_rendered_file() {

        Attributes attributes = Attributes.builder().copyCss(true).linkCss(false).build();
        Options options = Options.builder()
                .inPlace(true)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        try {
            File cssFile = new File(renderSample.getParent(), "asciidoctor.css");
            assertThat(cssFile.exists(), is(false));
        } catch (RuntimeException e) {
            assertThat(e.getCause(), is(instanceOf(FileNotFoundException.class)));
        }

    }

    @Test
    public void copycss_with_to_file_should_copy_css_to_to_file_directory() {

        Attributes attributes = Attributes.builder().linkCss(true).copyCss(true).build();
        Options options = Options.builder()
                .inPlace(false)
                .toFile(new File(tempFolder, "output.html"))
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        File cssFile = new File(tempFolder, "asciidoctor.css");
        assertThat(cssFile.exists(), is(true));

    }

    @Test
    public void copycss_with_to_dir_should_copy_css_to_to_dir_directory() {

        Attributes attributes = Attributes.builder().linkCss(true).copyCss(true).build();
        Options options = Options.builder()
                .inPlace(false)
                .toDir(tempFolder)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convertFile(renderSample, options);

        File cssFile = new File(tempFolder, "asciidoctor.css");
        assertThat(cssFile.exists(), is(true));

    }

    @Test
    public void copycss_with_render_to_file_should_copy_css_to_to_file_directory() {

        Attributes attributes = Attributes.builder().linkCss(true).copyCss(true).build();
        Options options = Options.builder()
                .inPlace(false)
                .toFile(new File(tempFolder, "output.html"))
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convert("This is Asciidoctor", options);

        File cssFile = new File(tempFolder, "asciidoctor.css");
        assertThat(cssFile.exists(), is(true));

    }

    @Test
    public void copycss_with_render_to_dir_should_copy_css_to_to_dir_directory() {

        Attributes attributes = Attributes.builder().linkCss(true).copyCss(true).build();
        Options options = Options.builder()
                .inPlace(false)
                .toDir(tempFolder)
                .safe(SafeMode.UNSAFE)
                .attributes(attributes)
                .build();

        asciidoctor.convert("This is Asciidoctor", options);

        File cssFile = new File(tempFolder, "asciidoctor.css");
        assertThat(cssFile.exists(), is(true));

    }

    @Test
    public void string_content_with_icons_enabled_should_be_rendered()
            throws IOException, SAXException, ParserConfigurationException {

        Attributes attributes = Attributes.builder()
                .icons(Attributes.IMAGE_ICONS)
                .build();
        Options options = Options.builder().attributes(attributes).build();

        String result = asciidoctor.convert(Files.readString(documentWithNote.toPath()), options);
        result = result.replaceAll("<img(.*?)>", "<img$1/>");
        assertRenderedAdmonitionIcon(result);

    }

    @Test
    public void string_content_with_fontawesome_icons_enabled_should_be_rendered()
            throws IOException, SAXException, ParserConfigurationException {

        InputStream content = new FileInputStream(documentWithNote);

        Attributes attributes = Attributes.builder()
                .icons(Attributes.FONT_ICONS)
                .build();
        Options options = Options.builder().attributes(attributes).build();

        String result = asciidoctor.convert(toString(content), options);
        assertRenderedFontAwesomeAdmonitionIcon(result);

    }

    @Test
    public void string_content_with_icons_enabled_and_iconsdir_set_should_be_rendered_with_iconsdir()
            throws IOException {

        InputStream content = new FileInputStream(documentWithNote);

        Attributes attributes = Attributes.builder()
                .icons(Attributes.IMAGE_ICONS)
                .iconsDir("icons")
                .build();
        Options options = Options.builder().attributes(attributes).build();

        String renderContent = asciidoctor.convert(toString(content), options);

        Document doc = Jsoup.parse(renderContent, "UTF-8");
        Elements image = doc.select("img[src]");
        String srcValue = image.attr("src");
        assertThat(srcValue, is("icons/note.png"));

    }

    @Test
    public void linkattrs_should_make_asciidoctor_render_link_macro_attributes() {

        Attributes attributes = Attributes.builder().linkAttrs(true).build();
        Options options = Options.builder().attributes(attributes).build();

        String content = asciidoctor.convert(
                "https://google.com[Google, window=\"_blank\"]", options);

        Document doc = Jsoup.parse(content);
        Elements image = doc.select("a[target]");

        String targetValue = image.attr("target");
        assertThat(targetValue, is("_blank"));

    }

    @Test
    public void experimental_flag_should_enable_experimental_features_like_keyboard_shortcuts() {

        Attributes attributes = Attributes.builder().experimental(true).build();
        Options options = Options.builder().attributes(attributes).build();

        String content = asciidoctor.convert("kbd:[F11]", options);

        Document doc = Jsoup.parse(content);
        Elements image = doc.select("kbd");

        assertThat(image.text(), is("F11"));
    }

    @Test
    public void iconfont_attributes_should_be_used_for_using_custom_font_css_icons()
            throws URISyntaxException, IOException {

        Attributes attributes = Attributes.builder().icons(Attributes.FONT_ICONS)
                .iconFontRemote(true).iconFontCdn(new URI("http://mycdn/css/font-awesome.min.css")).build();
        Options options = Options.builder().inPlace(true).attributes(attributes).build();

        asciidoctor.convertFile(documentWithNote, options);

        File expectedFile = new File(documentWithNote.getParent(), "documentwithnote.html");
        Document doc = Jsoup.parse(expectedFile, "UTF-8");

        Elements stylesheetLinks = doc
                .select("link[href=http://mycdn/css/font-awesome.min.css]");
        assertThat(stylesheetLinks.size(), is(1));

        expectedFile.delete();

    }

    private void assertRenderedFontAwesomeAdmonitionIcon(String renderContent)
            throws IOException, SAXException, ParserConfigurationException {

        Source renderFileSource = new DOMSource(
                inputStream2Document(new ByteArrayInputStream(
                        renderContent.getBytes())));
        assertThat(renderFileSource, hasXPath("//i[@class='fa icon-note']"));
    }

    private void assertRenderedAdmonitionIcon(String render_content)
            throws IOException, SAXException, ParserConfigurationException {
        Source renderFileSource = new DOMSource(
                inputStream2Document(new ByteArrayInputStream(
                        render_content.getBytes())));

        assertThat(renderFileSource, hasXPath("//img[@alt='Note']"));
    }

    private static String toString(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), UTF_8);
    }

    private static org.w3c.dom.Document inputStream2Document(
            InputStream inputStream) throws IOException, SAXException,
            ParserConfigurationException {
        DocumentBuilderFactory newInstance = DocumentBuilderFactory
                .newInstance();
        newInstance.setNamespaceAware(true);
        org.w3c.dom.Document parse = newInstance.newDocumentBuilder().parse(
                inputStream);
        return parse;
    }
}
