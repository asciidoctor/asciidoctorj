package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Specification

class WhenABlockMacroProcessorCreatesAList extends Specification {

    public static final String LISTMACRO_NAME = 'list'
    public static final String UTF_8 = 'UTF-8'
    public static final String OL = 'ol'
    public static final String UL = 'ul'
    public static final String LI = 'li'
    public static final String FIRST_ITEM = 'First item'
    public static final String SECOND_ITEM = 'Second item'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    private static final String DOCUMENT = '''
= Section Creation Test

list::HelloWorld[]
'''

    def "the ordered list should appear in the resulting document"() {

        given:
        asciidoctor.javaExtensionRegistry().blockMacro(LISTMACRO_NAME, new ListCreatorBlockMacro('olist'))

        when:
        String result = asciidoctor.convert(DOCUMENT, OptionsBuilder.options().safe(SafeMode.SAFE).toFile(false).standalone(false))

        then:
        noExceptionThrown()
        Document htmlDocument = Jsoup.parse(result, UTF_8)

        htmlDocument.select(OL).size() == 1
        def ol = htmlDocument.select(OL).first()
        ol.attr('start') == '42'
        def items = ol.select(LI)
        items.size() == 2
        items.get(0).text() == FIRST_ITEM
        items.get(1).text() == SECOND_ITEM
    }

    def "the unordered list should appear in the resulting document"() {

        given:
        asciidoctor.javaExtensionRegistry().blockMacro(LISTMACRO_NAME, new ListCreatorBlockMacro('ulist'))

        when:
        String result = asciidoctor.convert(DOCUMENT, OptionsBuilder.options().safe(SafeMode.SAFE).toFile(false).standalone(false))

        then:
        noExceptionThrown()
        Document htmlDocument = Jsoup.parse(result, UTF_8)

        htmlDocument.select(UL).size() == 1
        def ul = htmlDocument.select(UL).first()
        def items = ul.select(LI)
        items.size() == 2
        items.get(0).text() == FIRST_ITEM
        items.get(1).text() == SECOND_ITEM
    }
}
