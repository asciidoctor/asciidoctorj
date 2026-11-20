package org.asciidoctor.extension;

import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Contexts({Contexts.PARAGRAPH})
@Name("checkattributes")
public class AttributeCheckingBlockProcessor extends BlockProcessor {

    private final List<Object> attributeKeys = new ArrayList<>();

    public List<Object> getAttributeKeys() {
        return attributeKeys;
    }

    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        attributeKeys.addAll(attributes.keySet());
        return parent;
    }
}
