package org.asciidoctor.converter

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.PhraseNode

@ConverterFor(value = PhraseNodeConverter.DEFAULT_FORMAT, suffix = '.txt')
class PhraseNodeConverter extends StringConverter {

    static final String DEFAULT_FORMAT = 'inline'

    static final String NEWLINE = '\n'

    PhraseNodeConverter(String backend, Map<String, Object> opts) {
        super(backend, opts)
    }

    @Override
    String convert(ContentNode node, String transform, Map<Object, Object> opts) {
        if (transform == null) {
            transform = node.nodeName
        }

        this."convert${transform.capitalize()}"(node)
    }

    Object convertEmbedded(Document node) {
        node.blocks
                .collect { it.content }
                .join(NEWLINE)
    }

    Object convertParagraph(Block node) {
        node.blocks
                .collect { it.content }
                .join(NEWLINE)
    }

    Object convertInline_quoted(PhraseNode node) {
        ">>>${node.text}<<<"
    }

}
