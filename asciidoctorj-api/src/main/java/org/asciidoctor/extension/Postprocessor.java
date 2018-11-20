package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.Document;

public abstract class Postprocessor extends BaseProcessor {

    public Postprocessor() {
        this(new HashMap<>());
    }
    
    public Postprocessor(Map<String, Object> config) {
        super(config);
    }

    public abstract String process(Document document, String output);

}
