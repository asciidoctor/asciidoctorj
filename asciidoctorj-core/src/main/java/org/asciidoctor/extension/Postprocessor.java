package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.DocumentRuby;

public abstract class Postprocessor extends Processor {

    public Postprocessor() {
        this(new HashMap<String, Object>());
    }
    
    public Postprocessor(Map<String, Object> config) {
        super(config);
    }

    public abstract String process(DocumentRuby document, String output);

}
