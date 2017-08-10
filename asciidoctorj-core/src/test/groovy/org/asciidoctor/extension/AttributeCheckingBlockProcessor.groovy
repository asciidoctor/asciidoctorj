package org.asciidoctor.extension

import org.asciidoctor.ast.StructuralNode

@Contexts([Contexts.PARAGRAPH])
@Name('checkattributes')
class AttributeCheckingBlockProcessor extends BlockProcessor {


    @Override
    Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        attributes.keySet().each { assert it in String }
        parent
    }
}
