package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractBlock;

import java.util.Map;

public interface ConverterBuiltIn {

    Object convert(AbstractBlock node);
    Object convert(AbstractBlock node, String transform);
    Object convert(AbstractBlock node, String transform, Map<Object, Object> opts);
    String content(AbstractBlock node);
}
