package org.asciidoctor

import org.asciidoctor.ast.Document
import org.asciidoctor.ast.Table
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.jruby.exceptions.RaiseException
import org.junit.runner.RunWith

import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenATableIsLoaded extends Specification {


    @ArquillianResource
    private Asciidoctor asciidoctor

    def "colspan greater 1 should be passed correctly to the nodes"() {

        given:
        String document = '''= Test document

|===
| Column A | Column B | Column C

3.+|Same cell content in columns 1, 2, and 3
|===
'''

        when:
        Document documentNode = asciidoctor.load(document, OptionsBuilder.options().headerFooter(false).asMap())
        Table tableNode = documentNode.blocks[0]

        then:
        tableNode.body[0].cells[0].colspan == 3
    }


    def "normal columns should be passed as colspan 0 to the nodes"() {

        given:
        String document = '''= Test document

|===
| Column A

|One cell content in columns 1
|===
'''

        when:
        Document documentNode = asciidoctor.load(document, OptionsBuilder.options().headerFooter(false).asMap())
        Table tableNode = documentNode.blocks[0]

        then:
        tableNode.body[0].cells[0].colspan == 0
    }

	def "asking a table cell for its inner document when it does not have one should return null"() {

		given:
		String document = '''= Test document

[cols="40,60"]
|===
| Source | Output

a|

The first content cell

a|

The second content cell

|===
'''

		when:
		Document documentNode = asciidoctor.load(document, OptionsBuilder.options().headerFooter(false).asMap())
		Table tableNode = documentNode.blocks[0]

		then:
		tableNode.header[0].cells[0].innerDocument == null
		tableNode.header[0].cells[1].innerDocument == null
		notThrown(RaiseException)
	}
}
