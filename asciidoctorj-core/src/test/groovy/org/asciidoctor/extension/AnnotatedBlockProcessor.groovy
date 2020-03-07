package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.api.extension.BlockProcessor
import org.asciidoctor.api.extension.Contexts
import org.asciidoctor.api.extension.DefaultAttribute
import org.asciidoctor.api.extension.DefaultAttributes
import org.asciidoctor.api.extension.Name
import org.asciidoctor.api.extension.Reader

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
