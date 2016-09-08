package org.asciidoctor.extension

import org.asciidoctor.Asciidoctor
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.arquillian.test.api.ArquillianResource
import org.junit.runner.RunWith

import spock.lang.Specification

@RunWith(ArquillianSputnik)
class WhenABlockProcessorCreatesABlockThatATreeProcessorVisits extends Specification {

	@Name("tst")
	@Contexts(Contexts.CONTEXT_OPEN)
	@ContentModel(ContentModel.COMPOUND)
	public static class BlockCreator extends BlockProcessor {

		@Override
		public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
			List<String> output = new LinkedList<>();
			output.add("line 1");
			output.add("line 2");

			attributes.put("name", "value");

			return createBlock(parent, "open", output, attributes);
		}
	}

	public static class BlockVisitor extends Treeprocessor {

		@Override
		public Document process(Document document) {
			recurse(document);
			return document;
		}

		private void recurse(StructuralNode node) {
			// Accessing the attributes of a block that was previously created by a block processor
			// causes a ClassCastException in ContentNodeImpl#getAttributes since the value returned
			// from the AbstractNode in the JRuby AST is an instance of MapJavaProxy, which does not
			// conform to RubyHash.
			Map<String, Object> attributes = node.getAttributes();

			for (Block block : node.getBlocks())
				recurse(block);
		}
	}

    @ArquillianResource
    private Asciidoctor asciidoctor

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
        asciidoctor.convert(DOCUMENT, OptionsBuilder.options().safe(SafeMode.SAFE).toFile(false).headerFooter(true))

        then:
        notThrown(ClassCastException)
    }


}
