package org.asciidoctor

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Cell
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.List
import org.asciidoctor.ast.ListItem
import org.asciidoctor.ast.Table
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Specification

/**
 * Tests that the unsubstituted text can be retrieved from nodes
 */
@RunWith(ArquillianSputnik)
class WhenTheRawTextShouldBeRetrieved extends Specification {

    @ArquillianResource
    private Asciidoctor asciidoctor

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
        block.source == 'This paragraph should show {foo}'
        block.content == 'This paragraph should show bar'
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

}
