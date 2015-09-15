package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenABlockMacroProcessorCreatesASection extends Specification {

    public static final String BLOCKMACRO_NAME = 'section'

    @ArquillianResource
    private Asciidoctor asciidoctor

    private static final String DOCUMENT = '''
= Section Creation Test

section::HelloWorld[]
'''

    def "the section should appear in the resulting document"() {

        given:
        asciidoctor.javaExtensionRegistry().blockMacro(BLOCKMACRO_NAME, SectionCreatorBlockMacro)

        when:
        String result = asciidoctor.convert(DOCUMENT, OptionsBuilder.options().safe(SafeMode.SAFE).toFile(false).headerFooter(true))

        then:
        noExceptionThrown()
        Document htmlDocument = Jsoup.parse(result, 'UTF-8')

        htmlDocument.select('h2').text() == '1. HelloWorld'

        htmlDocument.select('div.sect1').select('div.paragraph').text() == SectionCreatorBlockMacro.CONTENT
    }


}
