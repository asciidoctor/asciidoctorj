package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.api.extension.BlockMacroProcessor
import org.asciidoctor.api.extension.Name

@CompileStatic
@Name('testmacro')
class AnnotatedBlockMacroProcessor extends BlockMacroProcessor {

    public static final String RESULT = 'This content is added by this macro!'

    @Override
    Object process(StructuralNode parent, String target, Map<String, Object> attributes) {
        createBlock(parent, 'paragraph', RESULT)
    }
}
