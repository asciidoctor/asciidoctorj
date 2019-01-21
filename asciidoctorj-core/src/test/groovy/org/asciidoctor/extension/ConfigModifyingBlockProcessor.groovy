package org.asciidoctor.extension

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.api.extension.BlockProcessor
import org.asciidoctor.api.extension.Reader

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
        setConfig([:])
    }
}
