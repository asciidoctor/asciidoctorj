package org.asciidoctor.extension;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenABlockProcessorCreatesABlockThatATreeProcessorVisitsTest {

    @Name("tst")
    @Contexts(Contexts.OPEN)
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

            // To silence Codenarc. We must access the attributes to provoke the error.
            attributes = new HashMap<String, Object>(attributes);

            for (StructuralNode block : node.getBlocks()) {
                recurse(block);
            }
        }
    }

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private static final String DOCUMENT = """

            = Block and Tree Processor Interaction Test

            [tst]
            --
            This will be ignored
            --

            """;

    @Test
    @DisplayName("execution should not throw class cast exception")
    public void executionShouldNotThrowClassCastException() {
        // given:
        asciidoctor.javaExtensionRegistry().block(BlockCreator.class);
        asciidoctor.javaExtensionRegistry().treeprocessor(BlockVisitor.class);

        // when:
        Options options = Options.builder().safe(SafeMode.SAFE).toFile(false).standalone(true).build();
        assertThatCode(() -> asciidoctor.convert(DOCUMENT, options))
        // then:
                .doesNotThrowAnyException();
    }

}
