package org.asciidoctor.integrationguide.converter;

//tag::include[]
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.converter.ConverterFor;
import org.asciidoctor.converter.StringConverter;

import java.util.Map;

@ConverterFor("text")                                                     // <1>
public class TextConverter extends StringConverter {

    private String LINE_SEPARATOR = "\n";

    public TextConverter(String backend, Map<String, Object> opts) {      // <2>
        super(backend, opts);
    }

    @Override
    public String convert(
            ContentNode node, String transform, Map<Object, Object> o) {  // <3>

        if (transform == null) {                                          // <4>
            transform = node.getNodeName();
        }

        if (node instanceof Document) {
            Document document = (Document) node;
            return document.getContent().toString();                      // <5>
        } else if (node instanceof Section) {
            Section section = (Section) node;
            return new StringBuilder()
                    .append("== ").append(section.getTitle()).append(" ==")
                    .append(LINE_SEPARATOR).append(LINE_SEPARATOR)
                    .append(section.getContent()).toString();             // <5>
        } else if (transform.equals("paragraph")) {
            StructuralNode block = (StructuralNode) node;
            String content = (String) block.getContent();
            return new StringBuilder(content.replaceAll(LINE_SEPARATOR, " "))
                    .append(LINE_SEPARATOR).toString();                   // <5>
        }
        return null;
    }

}
//end::include[]
