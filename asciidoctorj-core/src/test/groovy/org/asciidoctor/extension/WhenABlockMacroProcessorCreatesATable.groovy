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

    public static final String FIRST_TD = 'td:first-child'
    public static final String SECOND_TD = 'td:nth-child(2)'
    public static final String CONTRIBUTOR = 'bob'
    public static final String BLOCKMACRO_NAME = 'githubcontributors'
    public static final String ALIGN_ATTRIBUTE = '@align'
    @ArquillianResource
    private Asciidoctor asciidoctor

    @ArquillianResource
    private TemporaryFolder tmp

    private static final String DOCUMENT = '''
= AsciidoctorJ contributors

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

        htmlDocument.select(FIRST_TD).size() == htmlDocument.select(SECOND_TD) != 0
        htmlDocument.select(FIRST_TD).size() != 0

        htmlDocument.select(FIRST_TD).every { tdElement -> tdElement.hasClass('halign-left')}
        htmlDocument.select(SECOND_TD).every { tdElement -> tdElement.hasClass('halign-center')}

        htmlDocument.select(FIRST_TD).find { tdElement -> tdElement.text() == CONTRIBUTOR } != null
    }

    def "the table should be rendered to docbook"() {

        given:

        asciidoctor.javaExtensionRegistry().blockMacro(BLOCKMACRO_NAME, GithubContributorsBlockMacro)

        File resultFile = tmp.newFile('result.db')

        when:
        asciidoctor.convert(DOCUMENT,
                OptionsBuilder.options()
                        .backend('docbook')
                        .safe(SafeMode.SAFE)
                        .inPlace(false)
                        .baseDir(tmp.getRoot())
                        .toDir(tmp.getRoot())
                        .toFile(resultFile))

        then:
        def rootNode = new XmlSlurper().parse(resultFile)
        rootNode.table.tgroup.tbody.'*'.find { row ->
            row.entry[0].text() == CONTRIBUTOR
        }
        rootNode.table.tgroup.tbody.'*'.size() > 0
        rootNode.table.tgroup.tbody.'*'.every {row -> row.entry[0][ALIGN_ATTRIBUTE] == 'left'}
        rootNode.table.tgroup.tbody.'*'.every {row -> row.entry[1][ALIGN_ATTRIBUTE] == 'center'}
    }

}
