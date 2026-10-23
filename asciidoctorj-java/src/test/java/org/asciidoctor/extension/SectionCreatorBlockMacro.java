package org.asciidoctor.extension;

import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

@Name("section")
public class SectionCreatorBlockMacro extends BlockMacroProcessor {

    public static final String CONTENT = "This is just some text";

    @Override
    public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
        Section section = createSection(parent);
        section.setTitle(target);

        parseContent(section, List.of(CONTENT));

        return section;
    }
}
