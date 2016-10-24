package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.AbstractNode;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.ListNode;
import org.asciidoctor.ast.Section;

import java.util.Map;

public class TextConverter extends AbstractConverter {

    private String LINE_SEPARATOR = "\n";

    public TextConverter(String backend, Map<Object, Object> opts) {
        super(backend, opts);
    }
    
    @Override
    public Object convert(AbstractNode node, String transform, Map<Object, Object> o) {

        if (transform == null) {
            transform = node.getNodeName();
        }
 
        if (node instanceof DocumentRuby) {
            DocumentRuby document = (DocumentRuby) node;
            return document.getContent();
        } else if (node instanceof Section) {
            Section section = (Section) node;
            return new StringBuilder()
                    .append("== ").append(section.getTitle()).append(" ==")
                    .append(LINE_SEPARATOR).append(LINE_SEPARATOR)
                    .append(section.getContent()).toString();
        } else if (transform.equals("paragraph")) {
            AbstractBlock block = (AbstractBlock) node;
            String content = (String) block.getContent();
            return new StringBuilder(content.replaceAll(LINE_SEPARATOR, " ")).append(LINE_SEPARATOR);
        } else if (node instanceof ListNode) {
            StringBuilder sb = new StringBuilder();
            for (AbstractBlock listItem: ((ListNode) node).getItems()) {
                sb.append(listItem.convert()).append(LINE_SEPARATOR);
            }
            return sb.toString();
        } else if (node instanceof ListItem) {
            return "-> " + ((ListItem) node).getText();
        } else if (node instanceof AbstractBlock) {
            AbstractBlock block = (AbstractBlock) node;
            return block.getContent();
        }
        return null;
    }

}
