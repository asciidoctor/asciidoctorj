package org.asciidoctor.converter;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.Section;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

import java.util.Map;

@ConverterFor(value = TextConverter.DEFAULT_FORMAT, suffix = ".txt")
public class TextConverter extends StringConverter {

    public static final String DEFAULT_FORMAT = "annotatedtext";

    private String LINE_SEPARATOR = "\n";

    public TextConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
    }
    
    @Override
    public String convert(ContentNode node, String transform, Map<Object, Object> o) {


        if (transform == null) {
            transform = node.getNodeName();
        }
 
        if (node instanceof Document) {
            log(new LogRecord(Severity.INFO, "Now we're logging"));
            Document document = (Document) node;
            return (String) document.getContent();
        } else if (node instanceof Section) {
            Section section = (Section) node;
            return new StringBuilder()
                    .append("== ").append(section.getTitle()).append(" ==")
                    .append(LINE_SEPARATOR).append(LINE_SEPARATOR)
                    .append(section.getContent()).toString();
        } else if (transform.equals("paragraph")) {
            StructuralNode block = (StructuralNode) node;
            String content = (String) block.getContent();
            return new StringBuilder(content.replaceAll(LINE_SEPARATOR, " ")).append(LINE_SEPARATOR).toString();
        } else if (node instanceof List) {
            StringBuilder sb = new StringBuilder();
            for (StructuralNode listItem: ((List) node).getItems()) {
                sb.append(listItem.convert()).append(LINE_SEPARATOR);
            }
            return sb.toString();
        } else if (node instanceof ListItem) {
            return "-> " + ((ListItem) node).getText();
        } else if (node instanceof StructuralNode) {
            StructuralNode block = (StructuralNode) node;
            return block.getContent().toString();
        }
        return null;
    }

}
