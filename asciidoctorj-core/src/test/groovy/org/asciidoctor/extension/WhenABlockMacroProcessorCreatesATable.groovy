package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.util.ClasspathResources
import org.asciidoctor.util.TestHttpServer
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenABlockMacroProcessorCreatesATable extends Specification {

    public static final String FIRST_TD  = 'td:first-child'
    public static final String SECOND_TD = 'td:nth-child(2)'
    public static final String THIRD_TD  = 'td:nth-child(3)'
    public static final String IMG_ELEMENT  = 'img'
    public static final String CONTRIBUTOR = 'bob'
    public static final String BLOCKMACRO_NAME = 'githubcontributors'

    public static final String AVATAR_URL_REGEXP = /https:\/\/avatars.githubusercontent.com\/u\/.*/

    @ArquillianResource
    private Asciidoctor asciidoctor

    @ArquillianResource
    private TemporaryFolder tmp

    private static final String DOCUMENT = '''
= AsciidoctorJRuby contributors

githubcontributors::asciidoctor/asciidoctorj[]
'''

    @ArquillianResource
    private ClasspathResources classpathResources

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
        Document htmlDocument = Jsoup.parse(resultFile, 'UTF-8')

        htmlDocument.select('table').hasClass('grid-rows')

        htmlDocument.select(FIRST_TD).every { tdElement -> tdElement.select(IMG_ELEMENT).size() == 1 }
        htmlDocument.select(FIRST_TD).every { tdElement -> tdElement.select(IMG_ELEMENT)[0].attr('src') =~ AVATAR_URL_REGEXP }

        htmlDocument.select(SECOND_TD).size() == htmlDocument.select(SECOND_TD) != 0
        htmlDocument.select(SECOND_TD).size() != 0

        htmlDocument.select(SECOND_TD).every { tdElement -> tdElement.hasClass('halign-left')}
        htmlDocument.select(THIRD_TD).every { tdElement -> tdElement.hasClass('halign-center')}

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
