package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.ast.ContentNode

@CompileStatic
@Name('man')
@Format(regexp = 'manpage:(.*?)\\[(.*?)\\]')
class AnnotatedRegexpInlineMacroProcessor extends InlineMacroProcessor {

    AnnotatedRegexpInlineMacroProcessor(String macroName) {
        super(macroName)
    }

    @Override
    Object process(ContentNode parent, String target, Map<String, Object> attributes) {
        Map<String, Object> options = new HashMap<String, Object>()
        options['type'] = ':link'
        options['target'] = "${target}.html"
        createPhraseNode(parent, 'anchor', target, attributes, options)
    }
}