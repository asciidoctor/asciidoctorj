package org.asciidoctor

import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.DescriptionList
import org.asciidoctor.ast.DescriptionListEntry
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.ListItem
import org.asciidoctor.converter.StringConverter
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenADocumentContainsADefinitionList extends Specification {


    @ArquillianResource
    private Asciidoctor asciidoctor

    def "the definition list should be loaded"() {

        given:
        String document = '''= Test document

Item A::
Item B:: A description

Item C:: Another description
'''

        when:
        Document documentNode = asciidoctor.load(document, OptionsBuilder.options().headerFooter(false).asMap())

        then:
        DescriptionList list = documentNode.blocks[0]

        list.items != null || list.items.size() == 2

        list.items[0] instanceof DescriptionListEntry

        list.items[0].terms[0].text == 'Item A'
        list.items[0].terms[0] instanceof ListItem

        list.items[0].terms[1].text == 'Item B'
        list.items[0].terms[1] instanceof ListItem

        list.items[0].description instanceof ListItem
        list.items[0].description.hasText()
        list.items[0].description.text == 'A description'

        list.items[1] instanceof DescriptionListEntry

        list.items[1].terms[0].text == 'Item C'

        list.items[1].description instanceof ListItem
        list.items[1].description.hasText()
        list.items[1].description.text == 'Another description'
    }

    static class TestConverter extends StringConverter {

        static final String NAME = 'test'

        static final String NEWLINE = '\n'
        static final String NEWLINE_2 = NEWLINE * 2

        TestConverter(String backend, Map<String, Object> opts) {
            super(backend, opts)
        }

        @Override
        String convert(ContentNode node, String transform, Map<Object, Object> opts) {
            if (transform == null) {
                transform = node.nodeName
            }

            this."convert${transform.capitalize()}"(node)
        }

        Object convertEmbedded(Document node) {
            String result = node.doctitle.toUpperCase()
            String content = node.content
            if (content && content.trim().length() > 0) {
                result += NEWLINE_2 + content
            }
            result
        }

        Object convertDlist(DescriptionList dlist) {
            dlist.items.collect { listItem ->
                String items = listItem.terms*.text.join(', ')
                String description = listItem.description?.text
                items + NEWLINE + '----' + NEWLINE + (description == null ? '' : description + NEWLINE)
            }.join(NEWLINE_2)

        }

    }


    def "a document with a description list that contains no description should be converted"() {

        given:
        String document = '''= Test document

Item A::
'''

        when:
        asciidoctor.javaConverterRegistry().register(TestConverter, TestConverter.NAME)
        String result = asciidoctor.convert(document, OptionsBuilder.options().backend(TestConverter.NAME).headerFooter(false).asMap())

        then:
        result == '''TEST DOCUMENT

Item A
----
'''
    }

    def "a document with a description list should be converted"() {

        given:
        String document = '''= Test document

Item A::
Item B::
  A very nice description
'''

        when:
        asciidoctor.javaConverterRegistry().register(TestConverter, TestConverter.NAME)
        String result = asciidoctor.convert(document, OptionsBuilder.options().backend(TestConverter.NAME).headerFooter(false).asMap())

        then:
        result == '''TEST DOCUMENT

Item A, Item B
----
A very nice description
'''
    }

}
