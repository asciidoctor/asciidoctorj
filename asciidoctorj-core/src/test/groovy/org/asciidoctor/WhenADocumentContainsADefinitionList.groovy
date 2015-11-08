package org.asciidoctor

import org.asciidoctor.ast.DescriptionList
import org.asciidoctor.ast.DescriptionListEntry
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.ListItem
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
}
