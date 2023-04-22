package org.asciidoctor

import org.asciidoctor.ast.Document
import org.asciidoctor.ast.Table
import org.jruby.exceptions.RaiseException
import spock.lang.Specification

class WhenATableIsLoaded extends Specification {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    def "colspan greater 1 should be passed correctly to the nodes"() {

        given:
        String document = '''= Test document

|===
| Column A | Column B | Column C

3.+|Same cell content in columns 1, 2, and 3
|===
'''

        when:
        Document documentNode = asciidoctor.load(document, OptionsBuilder.options().standalone(false).asMap())
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
        Document documentNode = asciidoctor.load(document, OptionsBuilder.options().standalone(false).asMap())
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
        Document documentNode = asciidoctor.load(document, OptionsBuilder.options().standalone(false).asMap())
        Table tableNode = documentNode.blocks[0]

        then:
        tableNode.header[0].cells[0].innerDocument == null
        tableNode.header[0].cells[1].innerDocument == null
        notThrown(RaiseException)
    }
}
