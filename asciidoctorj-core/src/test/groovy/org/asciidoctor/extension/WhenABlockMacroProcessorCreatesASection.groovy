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
    public static final String UTF_8 = 'UTF-8'
    public static final String H2 = 'h2'
    public static final String HELLO_WORLD = '1. HelloWorld'
    public static final String SECT1_SELECTOR = 'div.sect1'
    public static final String PARAGRAPH_SELECTOR = 'div.paragraph'

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
        Document htmlDocument = Jsoup.parse(result, UTF_8)

        htmlDocument.select(H2).text() == HELLO_WORLD

        htmlDocument.select(SECT1_SELECTOR).select(PARAGRAPH_SELECTOR).text() == SectionCreatorBlockMacro.CONTENT
    }

    def "the section should appear in the resulting document when the extension is registered with an extension group"() {

        given:
        asciidoctor.createGroup().blockMacro(BLOCKMACRO_NAME, SectionCreatorBlockMacro).register()

        when:
        String result = asciidoctor.convert(DOCUMENT, OptionsBuilder.options().safe(SafeMode.SAFE).toFile(false).headerFooter(true))

        then:
        noExceptionThrown()
        Document htmlDocument = Jsoup.parse(result, UTF_8)

        htmlDocument.select(H2).text() == HELLO_WORLD

        htmlDocument.select(SECT1_SELECTOR).select(PARAGRAPH_SELECTOR).text() == SectionCreatorBlockMacro.CONTENT
    }


}
