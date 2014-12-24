package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractBlock;

import java.util.Map;

public interface Converter {
    Object convert(AbstractBlock abstractBlock, String s, Map<Object, Object> o);
}
