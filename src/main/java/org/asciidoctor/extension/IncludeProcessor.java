package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.DocumentRuby;

public abstract class IncludeProcessor extends Processor {

    public IncludeProcessor() {
        this(new HashMap<String, Object>());
    }
    
    public IncludeProcessor(Map<String, Object> config) {
        super(config);
    }

    public abstract boolean handles(String target);
    public abstract void process(DocumentRuby document, PreprocessorReader reader, String target, Map<String, Object> attributes);
    
}
