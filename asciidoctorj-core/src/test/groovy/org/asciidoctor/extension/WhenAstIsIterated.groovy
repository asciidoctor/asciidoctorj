package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.ast.Document
import spock.lang.Specification

class WhenAstIsIterated extends Specification {

    Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    final static DOCUMENT = '''
= Hello World

This is a test document

== This is the first section

This is a paragraph

== Another section

A list with items

* One
* Two
* Three
'''


    def "getDocument should always return the same instance"() {
        when:
        Document document = asciidoctor.load(DOCUMENT, [:])
        List<StructuralNode>  allBlocks = document.findBy([:])

        then:
        document.is(document.getDocument())
        allBlocks.every { block -> document.is(block.getDocument()) }
    }

    def "getParent should always return the same instance"() {
        when:
        Document document = asciidoctor.load(DOCUMENT, [:])
        def allBlocks = document.findBy([:])

        then: 'Every block of the document should return the same node as the parent from getParent()'
        allBlocks.every {
            block -> block.getBlocks().every { child -> block.is(child.getParent()) }
        }
    }

}
