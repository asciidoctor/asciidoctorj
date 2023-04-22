package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.jsoup.Jsoup
import spock.lang.Specification

class WhenATreeProcessorWorksOnTables extends Specification {

    private static final String EMPTY_DOCUMENT = '= Document without table'

    public static final String TABLE_BODY = 'tbody'

    public static final String TABLE_HEADER = 'thead'

    public static final String TABLE_FOOTER = 'tfoot'

    public static final String TD = 'td'

    public static final String TH = 'th'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    def "then the extension should be able to create tables"() {

        given: 'an empty document and a tree processor that creates a simple table'
        asciidoctor.javaExtensionRegistry().treeprocessor(TableCreatorTreeProcessor)

        when: 'the document is rendered'
        String content = asciidoctor.convert(EMPTY_DOCUMENT, OptionsBuilder.options().standalone(false))

        then: 'the resulting document has a table'
        org.jsoup.nodes.Document document = Jsoup.parse(content)

        document.getElementsByTag(TABLE_BODY).size() == 1

        and: 'this table has one cell with the text A1'

        org.jsoup.nodes.Element table = document.getElementsByTag(TABLE_BODY)[0]
        table.getElementsByTag(TD).size() == 1
        table.getElementsByTag(TD)[0].text() == 'A1'

        and: 'this table has a header row'

        org.jsoup.nodes.Element tableHeader = document.getElementsByTag(TABLE_HEADER)[0]
        tableHeader.getElementsByTag(TH).size() == 1
        tableHeader.getElementsByTag(TH)[0].text() == 'A'

        and: 'this table has a header footer'

        org.jsoup.nodes.Element tableFooter = document.getElementsByTag(TABLE_FOOTER)[0]
        tableFooter.getElementsByTag(TD).size() == 1
        tableFooter.getElementsByTag(TD)[0].text() == 'Sum of A'

    }

    def 'then it should be able to update the text of a cell'() {

        given: 'an document with a table and a tree processor that updates tables'
        asciidoctor.javaExtensionRegistry().treeprocessor(TableUpdaterTreeProcessor)

        when: 'the document is rendered'
        String content = asciidoctor.convert('''=== Test

|===
| Hello | World
| World | Hello 
|===

''', OptionsBuilder.options().standalone(false))

        def htmlDocument = Jsoup.parse(content)

        then:
        def replacedElements = htmlDocument.select('td p strong')
        replacedElements.size() == 4
        replacedElements.every { it.text() == 'Replaced' }
    }
}
