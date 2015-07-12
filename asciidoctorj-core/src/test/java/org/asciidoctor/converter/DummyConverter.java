package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractNode;

import java.util.Map;

public class DummyConverter extends AbstractConverter<String> {

    public DummyConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
    }
    
    @Override
    public String convert(AbstractNode node, String transform, Map<Object, Object> o) {
        return "Dummy";
    }

}
