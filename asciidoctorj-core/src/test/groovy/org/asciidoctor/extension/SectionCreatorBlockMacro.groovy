package org.asciidoctor.extension

import org.asciidoctor.ast.Section
import org.asciidoctor.ast.StructuralNode

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
