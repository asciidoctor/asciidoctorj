package org.asciidoctor.extension

import org.asciidoctor.ast.AbstractBlock
import org.asciidoctor.ast.Section

@Name('section')
class SectionCreatorBlockMacro extends BlockMacroProcessor {

    public static final String CONTENT = 'This is just some text'

    @Override
    Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        Section section = createSection(parent)
        section.title = target

        parseContent(section, [CONTENT])

        section
    }
}
