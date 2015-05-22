package org.asciidoctor.converter;

import org.asciidoctor.ast.BlockNode;
import org.asciidoctor.ast.Node;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.Section;

import java.util.Map;

public class TextConverter extends AbstractConverter {

    private String LINE_SEPARATOR = "\n";

    public TextConverter(String backend, Map<Object, Object> opts) {
        super(backend, opts);
    }
    
    @Override
    public Object convert(Node node, String transform, Map<Object, Object> o) {

        if (transform == null) {
            transform = node.getNodeName();
        }
 
        if (node instanceof Document) {
            Document document = (Document) node;
            return document.getContent();
        } else if (node instanceof Section) {
            Section section = (Section) node;
            return new StringBuilder()
                    .append("== ").append(section.getTitle()).append(" ==")
                    .append(LINE_SEPARATOR).append(LINE_SEPARATOR)
                    .append(section.getContent()).toString();
        } else if (transform.equals("paragraph")) {
            BlockNode block = (BlockNode) node;
            String content = (String) block.content();
            return new StringBuilder(content.replaceAll(LINE_SEPARATOR, " ")).append(LINE_SEPARATOR);
        } else if (node instanceof List) {
            StringBuilder sb = new StringBuilder();
            for (BlockNode listItem: ((List) node).getItems()) {
                sb.append(listItem.convert()).append(LINE_SEPARATOR);
            }
            return sb.toString();
        } else if (node instanceof ListItem) {
            return "-> " + ((ListItem) node).getText();
        } else if (node instanceof BlockNode) {
            BlockNode block = (BlockNode) node;
            return block.getContent();
        }
        return null;
    }

}
