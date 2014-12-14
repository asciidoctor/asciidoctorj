package org.asciidoctor.converter;

import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;

public class TextConverter implements Converter {
    private String LINE_SEPARATOR = "\n";

    public TextConverter(String backend, Map<Object, Object> opts) {
    }
    
    @Override
    public Object convert(AbstractBlock node) {
        return convert(node, null);
    }

    @Override
    public Object convert(AbstractBlock node, String transform) {
        if (transform == null) {
            transform = node.nodeName();
        }
 
        if (transform.equals("document")) {
            return node.content();
        } else if (transform.equals("section")) {
            return new StringBuilder().append("== ").append(node.title()).append(" ==").append(LINE_SEPARATOR).append(LINE_SEPARATOR).append(node.content()).toString();
        } else if (transform.equals("paragraph")) {
            String content = (String) node.content();
            return new StringBuilder(content.replaceAll(LINE_SEPARATOR, " ")).append('\n');
        } else {
            return node.content();
        }
    }
}
