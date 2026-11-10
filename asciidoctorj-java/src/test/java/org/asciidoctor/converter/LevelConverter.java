package org.asciidoctor.converter;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.Map;

public class LevelConverter extends StringConverter {

    public LevelConverter(String backend, Map<String, Object> opts) {      // <2>
        super(backend, opts);
    }

    @Override
    public String convert(
            ContentNode node, String transform, Map<Object, Object> o) {

        if (node instanceof Document) {
            Document document = (Document) node;
            return document.getContent().toString();
        } else if (node instanceof Section) {
            Section section = (Section) node;
            return new StringBuilder()
                    .append("== ").append(section.getLevel()).append(" ").append(section.getTitle()).append(" ==")
                    .toString();
        }
        return null;
    }

}
