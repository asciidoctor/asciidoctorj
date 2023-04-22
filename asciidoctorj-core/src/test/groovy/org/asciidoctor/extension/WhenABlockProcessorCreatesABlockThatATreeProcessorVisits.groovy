package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import spock.lang.Specification

class WhenABlockProcessorCreatesABlockThatATreeProcessorVisits extends Specification {

    @Name('tst')
    @Contexts(Contexts.OPEN)
    @ContentModel(ContentModel.COMPOUND)
    static class BlockCreator extends BlockProcessor {

        @Override
        Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
            List<String> output = new LinkedList<>()
            output.add('line 1')
            output.add('line 2')

            attributes.put('name', 'value')

            createBlock(parent, 'open', output, attributes)
        }
    }

    static class BlockVisitor extends Treeprocessor {

        @Override
        Document process(Document document) {
            recurse(document)
            document
        }

        private void recurse(StructuralNode node) {
            // Accessing the attributes of a block that was previously created by a block processor
            // causes a ClassCastException in ContentNodeImpl#getAttributes since the value returned
            // from the AbstractNode in the JRuby AST is an instance of MapJavaProxy, which does not
            // conform to RubyHash.
            Map<String, Object> attributes = node.getAttributes()

            // To silence Codenarc. We must access the attributes to provoke the error.
            attributes = new HashMap<String, Object>(attributes)

            for (Block block : node.getBlocks()) {
                recurse(block)
            }
        }
    }

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    private static final String DOCUMENT = '''
= Block and Tree Processor Interaction Test

[tst]
--
This will be ignored
--

'''

    def "execution should not throw class cast exception"() {
        given:
        asciidoctor.javaExtensionRegistry().block(BlockCreator)
        asciidoctor.javaExtensionRegistry().treeprocessor(BlockVisitor)

        when:
        asciidoctor.convert(DOCUMENT, OptionsBuilder.options().safe(SafeMode.SAFE).toFile(false).standalone(true))

        then:
        notThrown(ClassCastException)
    }

}
