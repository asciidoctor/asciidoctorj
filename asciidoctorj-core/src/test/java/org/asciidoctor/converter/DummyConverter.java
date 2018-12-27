package org.asciidoctor.converter;

import org.asciidoctor.api.ast.ContentNode;
import org.asciidoctor.api.converter.StringConverter;

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
