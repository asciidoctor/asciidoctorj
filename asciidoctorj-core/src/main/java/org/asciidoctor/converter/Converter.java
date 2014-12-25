package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractNode;

import java.util.Map;

public interface Converter {
    Object convert(AbstractNode abstractBlock, String s, Map<Object, Object> o);
}
