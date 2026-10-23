package org.asciidoctor.extension;

import java.util.Map;

import org.asciidoctor.ast.StructuralNode;

@Name("testmacro")
public class AnnotatedBlockMacroProcessor extends BlockMacroProcessor {

    public static final String RESULT = "This content is added by this macro!";

    @Override
    public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
        return createBlock(parent, "paragraph", RESULT);
    }
}
