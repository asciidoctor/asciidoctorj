package org.asciidoctor.extension

import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.api.extension.BlockProcessor
import org.asciidoctor.api.extension.Contexts
import org.asciidoctor.api.extension.Name
import org.asciidoctor.api.extension.Reader

@Contexts([Contexts.PARAGRAPH])
@Name('checkattributes')
class AttributeCheckingBlockProcessor extends BlockProcessor {


    @Override
    Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        attributes.keySet().each { assert it in String }
        parent
    }
}
