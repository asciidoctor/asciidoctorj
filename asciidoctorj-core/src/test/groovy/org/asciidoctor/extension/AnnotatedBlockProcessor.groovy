package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.ast.AbstractBlock

@CompileStatic
@Name('yell')
@Contexts([Contexts.CONTEXT_LISTING, Contexts.CONTEXT_OPEN])
@ContentModel(ContentModel.SIMPLE)
@DefaultAttributes([
    @DefaultAttribute(key = 'key', value = 'value')
])
class AnnotatedBlockProcessor extends BlockProcessor {

    AnnotatedBlockProcessor(String blockName) {
        super(blockName)
    }

    @Override
    Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
        assert attributes['key'] == 'value'
        List<String> lines = reader.readLines()
        List<String> newLines = lines*.toUpperCase()

        createBlock(parent, 'paragraph', newLines)
    }
}
