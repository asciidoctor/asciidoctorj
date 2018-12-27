package org.asciidoctor.extension

import org.asciidoctor.api.ast.Section
import org.asciidoctor.api.ast.StructuralNode
import org.asciidoctor.api.extension.BlockMacroProcessor
import org.asciidoctor.api.extension.Name

@Name('section')
class SectionCreatorBlockMacro extends BlockMacroProcessor {

    public static final String CONTENT = 'This is just some text'

    @Override
    Object process(StructuralNode parent, String target, Map<String, Object> attributes) {
        Section section = createSection(parent)
        section.title = target

        parseContent(section, [CONTENT])

        section
    }
}
