package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode

@CompileStatic
@Name('yell')
@Contexts([Contexts.LISTING, Contexts.OPEN])
@ContentModel(ContentModel.SIMPLE)
@DefaultAttributes([
    @DefaultAttribute(key = 'key', value = 'value')
])
class AnnotatedBlockProcessor extends BlockProcessor {

    AnnotatedBlockProcessor() {}

    // This constructor will not be called when registered as a a class
    // because 2 Strings don't match any known signature.
    AnnotatedBlockProcessor(String dummyValue, String blockName) {
        super(blockName)
    }

    @Override
    Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        assert attributes['key'] == 'value'
        List<String> lines = reader.readLines()
        List<String> newLines = lines*.toUpperCase()

        createBlock(parent, 'paragraph', newLines)
    }
}
