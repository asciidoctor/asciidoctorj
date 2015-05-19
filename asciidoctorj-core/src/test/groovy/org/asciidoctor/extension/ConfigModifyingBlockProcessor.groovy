package org.asciidoctor.extension

import org.asciidoctor.ast.AbstractBlock

class ConfigModifyingBlockProcessor extends BlockProcessor {

    ConfigModifyingBlockProcessor() {
        super('modify')
    }

    ConfigModifyingBlockProcessor(String name) {
        super(name)

        Map<String, Object> config = [:]
        config[BlockProcessor.CONTEXTS] = [BlockProcessor.CONTEXT_PARAGRAPH]
        config[Processor.CONTENT_MODEL] = Processor.CONTENT_MODEL_SIMPLE
        this.config = config
    }

    @Override
    Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
        setConfig([:])
    }
}
