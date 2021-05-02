package org.asciidoctor.extension

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode

class ConfigModifyingBlockProcessor extends BlockProcessor {

    ConfigModifyingBlockProcessor() {
        super('modify')
    }

    ConfigModifyingBlockProcessor(String name) {
        super(name)

        Map<String, Object> config = [:]
        config[Contexts.KEY] = [Contexts.PARAGRAPH]
        config[ContentModel.KEY] = ContentModel.SIMPLE
        this.config = config
    }

    @Override
    Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        config = [:]
    }
}
