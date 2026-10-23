package org.asciidoctor.converter;

import org.asciidoctor.ast.ContentNode;

import java.util.Map;

public class DummyConverter extends StringConverter {

    public DummyConverter(String backend, Map<String, Object> opts) {
        super(backend, opts);
    }
    
    @Override
    public String convert(ContentNode node, String transform, Map<Object, Object> o) {
        return "Dummy";
    }

}
