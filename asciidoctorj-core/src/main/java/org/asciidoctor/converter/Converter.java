package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractBlock;

public interface Converter {
    Object convert(AbstractBlock node);
    Object convert(AbstractBlock node, String transform);
}
