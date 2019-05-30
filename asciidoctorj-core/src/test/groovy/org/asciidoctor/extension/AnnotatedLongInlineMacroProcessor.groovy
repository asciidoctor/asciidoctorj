package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.ast.ContentNode

@CompileStatic
@Name('man')
@Format(FormatType.LONG)
@PositionalAttributes(['section', 'subsection'])
class AnnotatedLongInlineMacroProcessor extends InlineMacroProcessor {

    public static final String SUBSECTION = 'subsection'
    public static final String SECTION = 'section'

    @Override
    Object process(ContentNode parent, String target, Map<String, Object> attributes) {
        assert attributes[SECTION] == '7' || ( attributes[SECTION] == '8' || attributes[SUBSECTION] == '1')

        Map<String, Object> options = new HashMap<String, Object>()
        options['type'] = ':link'
        options['target'] = "${target}.html"
        createPhraseNode(parent, 'anchor', target, attributes, options)
    }
}