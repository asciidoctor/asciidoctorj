package org.asciidoctor.extension;

import java.util.Map;

import org.asciidoctor.ast.StructuralNode;

@Contexts({Contexts.PARAGRAPH})
@Name("checkattributes")
public class AttributeCheckingBlockProcessor extends BlockProcessor {


    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        for (Object it : attributes.keySet()) {
            assert it instanceof String;
        }
        return parent;
    }
}
