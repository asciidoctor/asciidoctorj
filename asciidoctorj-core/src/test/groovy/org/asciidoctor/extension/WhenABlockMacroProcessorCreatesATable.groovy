package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.util.ClasspathResources
import org.asciidoctor.util.TestHttpServer
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import spock.lang.Specification

import static java.nio.charset.StandardCharsets.UTF_8

@RunWith(ArquillianSputnik)
class WhenABlockMacroProcessorCreatesATable extends Specification {

    public static final String FIRST_TD  = 'td:first-child'
    public static final String SECOND_TD = 'td:nth-child(2)'
    public static final String THIRD_TD  = 'td:nth-child(3)'
    public static final String IMG_ELEMENT  = 'img'
    public static final String COL  = 'col'
    public static final String STYLE  = 'style'
    public static final String WIDTH  = 'width'
    public static final String WIDTH_2  = '2%'
    public static final String WIDTH_3  = '3%'
    public static final String WIDTH_20  = '20%'
    public static final String WIDTH_40  = '40%'
    public static final String CONTRIBUTOR = 'bob'
    public static final String BLOCKMACRO_NAME = 'githubcontributors'

    public static final String AVATAR_URL_REGEXP = /https:\/\/avatars.githubusercontent.com\/u\/.*/
    public static final String CSS_QUERY_TABLE = 'table'
    public static final String CLASS_GRID_ROWS = 'grid-rows'
    public static final String CLASS_HALIGN_LEFT = 'halign-left'
    public static final String CLASS_HALIGN_CENTER = 'halign-center'
    public static final String ATTR_SRC = 'src'
    public static final int THREE = 3

    @ArquillianResource
    private Asciidoctor asciidoctor

    @ArquillianResource
    private TemporaryFolder tmp

    @ArquillianResource
    private ClasspathResources classpathResources

    private static final String DOCUMENT = '''
= AsciidoctorJRuby contributors

githubcontributors::asciidoctor/asciidoctorj[]
'''

    private static final String DOCUMENT_WITH_NEGATIVE_WIDTHS = '''
= AsciidoctorJRuby contributors

githubcontributors::asciidoctor/asciidoctorj[widths="2,3,-1"]
'''


    def setup() {
        TestHttpServer.start(['http://api.github.com/repos/asciidoctor/asciidoctorj/contributors' : classpathResources.getResource('githubcontributors.json')])
    }

    def cleanup() {
        TestHttpServer.instance.stop()
    }

    def "the table should be rendered to html"() {

        given:

        asciidoctor.javaExtensionRegistry().blockMacro(BLOCKMACRO_NAME, GithubContributorsBlockMacro)

        File resultFile = tmp.newFile('result.html')

        when:
        asciidoctor.convert(DOCUMENT, OptionsBuilder.options().safe(SafeMode.SAFE).inPlace(false).baseDir(tmp.getRoot()).toDir(tmp.getRoot()).toFile(resultFile))

        then:
        Document htmlDocument = Jsoup.parse(resultFile, UTF_8.name())

        Elements cols = htmlDocument.select(COL)
        cols.size() == THREE
        cols.get(0).attr(STYLE).contains(WIDTH_20) || cols.get(0).attr(WIDTH).equals(WIDTH_20)
        cols.get(1).attr(STYLE).contains(WIDTH_40) || cols.get(1).attr(WIDTH).equals(WIDTH_40)
        cols.get(2).attr(STYLE).contains(WIDTH_40) || cols.get(2).attr(WIDTH).equals(WIDTH_40)

        htmlDocument.select(CSS_QUERY_TABLE).hasClass(CLASS_GRID_ROWS)

        htmlDocument.select(FIRST_TD).every { tdElement -> tdElement.select(IMG_ELEMENT).size() == 1 }
        htmlDocument.select(FIRST_TD).every { tdElement -> tdElement.select(IMG_ELEMENT)[0].attr(ATTR_SRC) =~ AVATAR_URL_REGEXP }

        htmlDocument.select(SECOND_TD).size() == htmlDocument.select(SECOND_TD) != 0
        htmlDocument.select(SECOND_TD).size() != 0

        htmlDocument.select(SECOND_TD).every { tdElement -> tdElement.hasClass(CLASS_HALIGN_LEFT) }
        htmlDocument.select(THIRD_TD).every { tdElement -> tdElement.hasClass(CLASS_HALIGN_CENTER) }

        htmlDocument.select(SECOND_TD).find { tdElement -> tdElement.text() == CONTRIBUTOR } != null
    }

    def "the table should be rendered to html with negative widths"() {
        given:
        asciidoctor.javaExtensionRegistry().blockMacro(BLOCKMACRO_NAME, GithubContributorsBlockMacro)
        File resultFile = tmp.newFile('resultWithNegativeWidth.html')

        when:
        def options = Options.builder()
                .safe(SafeMode.SAFE)
                .inPlace(false)
                .baseDir(tmp.getRoot())
                .toFile(resultFile)
                .build()
        asciidoctor.convert(DOCUMENT_WITH_NEGATIVE_WIDTHS, options)

        then:
        Document htmlDocument = Jsoup.parse(resultFile, UTF_8.name())

        Elements cols = htmlDocument.select(COL)
        cols.size() == THREE
        cols.get(0).attr(STYLE).contains(WIDTH_2) || cols.get(0).attr(WIDTH).equals(WIDTH_2)
        cols.get(1).attr(STYLE).contains(WIDTH_3) || cols.get(1).attr(WIDTH).equals(WIDTH_3)
        cols.get(2).attr(STYLE).length() == 0 && cols.get(2).attr(WIDTH).length() == 0

        htmlDocument.select(CSS_QUERY_TABLE).hasClass(CLASS_GRID_ROWS)

        htmlDocument.select(FIRST_TD).every { tdElement -> tdElement.select(IMG_ELEMENT).size() == 1 }
        htmlDocument.select(FIRST_TD).every { tdElement -> tdElement.select(IMG_ELEMENT)[0].attr(ATTR_SRC) =~ AVATAR_URL_REGEXP }

        htmlDocument.select(SECOND_TD).size() == htmlDocument.select(SECOND_TD) != 0
        htmlDocument.select(SECOND_TD).size() != 0

        htmlDocument.select(SECOND_TD).every { tdElement -> tdElement.hasClass(CLASS_HALIGN_LEFT) }
        htmlDocument.select(THIRD_TD).every { tdElement -> tdElement.hasClass(CLASS_HALIGN_CENTER) }

        htmlDocument.select(SECOND_TD).find { tdElement -> tdElement.text() == CONTRIBUTOR } != null
    }

    def "the table should be rendered to docbook"() {

        given:

        asciidoctor.javaExtensionRegistry().blockMacro(BLOCKMACRO_NAME, GithubContributorsBlockMacro)

        File resultFile = tmp.newFile('result.db')

        when:
        asciidoctor.convert(DOCUMENT,
                OptionsBuilder.options()
                        .backend('docbook5')
                        .safe(SafeMode.SAFE)
                        .inPlace(false)
                        .baseDir(tmp.getRoot())
                        .toDir(tmp.getRoot())
                        .toFile(resultFile))

        then:
        def rootNode = new XmlSlurper().parse(resultFile)
        rootNode.table.tgroup.tbody.'*'.find { row ->
            row.entry[1].text() == CONTRIBUTOR
        }
        rootNode.table.tgroup.tbody.'*'.size() > 0
        rootNode.table.tgroup.tbody.'*'.every {row ->
            row.entry[0].informalfigure.mediaobject.imageobject.imagedata.@fileref =~ AVATAR_URL_REGEXP
        }
        rootNode.table.tgroup.tbody.'*'.every {row -> row.entry[1].@align == 'left'}
        rootNode.table.tgroup.tbody.'*'.every {row -> row.entry[2].@align == 'center'}
    }

}
