package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractNode;

import java.util.Map;

public class DummyConverter extends AbstractConverter {

    public DummyConverter(String backend, Map<Object, Object> opts) {
        super(backend, opts);
    }
    
    @Override
    public Object convert(AbstractNode node, String transform, Map<Object, Object> o) {
        return "Dummy";
    }

}
