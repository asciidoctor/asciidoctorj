package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.DocumentRuby
import org.asciidoctor.ast.Section
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenAnExtensionAppendsChildBlocks extends Specification {

    String document = '''= Test document

== Section 1

some text

== Section 2

more text

'''

    @ArquillianResource
    private Asciidoctor asciidoctor

    def 'should be able to blocks via Block_append'() {

        given:

        final String additionalText = 'Text added by Treeprocessor'
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {

            int lastid = 0

            @Override
            DocumentRuby process(DocumentRuby document) {
                document.blocks.findAll { block -> block instanceof Section }.each {
                    block ->
                        Block newBlock = createBlock(block, 'paragraph', additionalText, [:])
                        newBlock.id = "NewBlock_${lastid++}"
                        block.append(newBlock)
                }
                document
            }
        })

        when:
        String result = asciidoctor.convert(this.document, OptionsBuilder.options().headerFooter(false))

        then:
        Document htmlDocument = Jsoup.parse(result)
        !htmlDocument.select('#NewBlock_0').empty
        !htmlDocument.select('#NewBlock_1').empty
        htmlDocument.select('#NewBlock_2').empty

    }

}
