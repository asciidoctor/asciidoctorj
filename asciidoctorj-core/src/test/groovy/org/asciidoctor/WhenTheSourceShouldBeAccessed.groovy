package org.asciidoctor

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Cell
import org.asciidoctor.ast.DescriptionList
import org.asciidoctor.ast.DescriptionListEntry
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.List
import org.asciidoctor.ast.ListItem
import org.asciidoctor.ast.Table
import org.asciidoctor.extension.Treeprocessor
import org.jsoup.Jsoup
import spock.lang.Specification

/**
 * Tests that the unsubstituted text can be retrieved from nodes
 */
class WhenTheSourceShouldBeAccessed extends Specification {

    public static final String SOURCE = 'This paragraph should show {foo}'
    public static final String CONVERTED = 'This paragraph should show bar'
    public static final String P = 'p'
    public static final String TD = 'td'

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    def 'it should be possible to get the raw text from a paragraph'() {

        given:
        String document = '''
= Test
:foo: bar

== Section

This paragraph should show {foo}

'''
        when:
        Document doc = asciidoctor.load(document, OptionsBuilder.options().asMap())
        Block block = doc.blocks[0].blocks[0]

        then:
        block.source == SOURCE
        block.content == CONVERTED
    }

    def 'it should be possible to get the raw text from a list item'() {

        given:
        String document = '''
= Test
:foo: bar

== Section

* This list item should show {foo}
  and should continue here
* This does not interest at all

'''
        when:
        Document doc = asciidoctor.load(document, OptionsBuilder.options().asMap())
        List list = doc.blocks[0].blocks[0]
        ListItem listItem = list.items[0]

        then:
        listItem.source == '''This list item should show {foo}
and should continue here'''
        listItem.text == '''This list item should show bar
and should continue here'''
        list.blocks.size() == list.items.size()
        list.items[0].source == list.blocks[0].source
    }

    def 'it should be possible to get the raw text from a description list item'() {

        given:
        String document = '''
= Test
:foo: bar

== Section

This::
  list item should show {foo}
  and should continue here
That::
  does not interest at all

'''
        when:
        Document doc = asciidoctor.load(document, OptionsBuilder.options().asMap())
        DescriptionList list = doc.blocks[0].blocks[0]
        DescriptionListEntry listItem = list.items[0]

        then:
        listItem.description.source == '''list item should show {foo}
and should continue here'''
        listItem.description.text == '''list item should show bar
and should continue here'''
        list.blocks.size() == 0
    }

    def 'it should be possible to get the raw text from a table cell'() {

        given:
        String document = '''
= Test
:foo: bar

== Section

|===
| Hello {foo}
|===

'''
        when:
        Document doc = asciidoctor.load(document, OptionsBuilder.options().asMap())
        Table table = doc.blocks[0].blocks[0]
        Cell cell = table.body[0].cells[0]

        then:
        cell.source == 'Hello {foo}'
        cell.text == 'Hello bar'
    }


    def 'it should be possible to set the raw text of a paragraph'() {

        given:
        String document = '''
= Test
:foo: bar

== Section

xxx

'''
        when:
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            Document process(Document doc) {
                doc.blocks[0].blocks[0].source = SOURCE
                doc
            }
        })
        String html = asciidoctor.convert(document, OptionsBuilder.options().standalone(false).asMap())
        org.jsoup.nodes.Document doc = Jsoup.parse(html)

        then:
        doc.select(P).text() == CONVERTED
    }

    def 'it should be possible to set the source of a list item'() {

        given:
        String document = '''
= Test
:foo: bar

== Section

* xxx
* This does not interest at all

'''
        when:
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            Document process(Document doc) {
                doc.blocks[0].blocks[0].items[0].source = SOURCE
                doc
            }
        })
        String html = asciidoctor.convert(document, OptionsBuilder.options().standalone(false).asMap())
        org.jsoup.nodes.Document doc = Jsoup.parse(html)

        then:
        doc.select(P).get(0).text() == CONVERTED
        doc.select(P).get(1).text() == 'This does not interest at all'
    }

    def 'it should be possible to set the raw text of a table cell'() {

        given:
        String document = '''
= Test
:foo: bar

== Section

|===
| xxx
|===

'''
        when:
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            Document process(Document doc) {
                Table table = doc.blocks[0].blocks[0]
                Cell cell = table.body[0].cells[0]
                cell.source = SOURCE
                doc
            }
        })
        String html = asciidoctor.convert(document, OptionsBuilder.options().standalone(false).asMap())
        org.jsoup.nodes.Document doc = Jsoup.parse(html)

        then:
        doc.select(TD).text() == CONVERTED
    }

}
