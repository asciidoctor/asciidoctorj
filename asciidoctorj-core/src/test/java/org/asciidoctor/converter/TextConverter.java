package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractBlock;

import java.util.HashMap;
import java.util.Map;

public class TextConverter implements ConverterBuiltIn {


    private String LINE_SEPARATOR = System.getProperty("line.separator");

    public TextConverter(String backend, Map<Object, Object> opts) {
    }

    @Override
    public Object convert(AbstractBlock node) {
        return convert(node, null);
    }

    @Override
    public Object convert(AbstractBlock node, String transform) {
        return convert(node, transform, new HashMap<Object, Object>());
    }

    @Override
    public Object convert(AbstractBlock node, String transform, Map<Object, Object> opts) {
        String nodeName = node.nodeName();
        String transformationRule = transform != null ? transform : node.nodeName();

        if (transformationRule.equals("document")) {
            return node.content();
        } else if (transformationRule.equals("section")) {
            return new StringBuilder().append("!!!").append(node.title()).append(LINE_SEPARATOR).append(LINE_SEPARATOR).append(node.content()).toString();
        } else if (transformationRule.equals("paragraph")) {
            String content = (String) node.content();
            return new StringBuilder(content.replaceAll(LINE_SEPARATOR, " ")).append('\n');
        }

        return node.content();

    }

    @Override
    public String content(AbstractBlock node) {
        return (String) node.content();
    }
}
