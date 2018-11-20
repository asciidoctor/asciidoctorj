package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.Document;

public abstract class IncludeProcessor extends BaseProcessor {

    public IncludeProcessor() {
        this(new HashMap<>());
    }
    
    public IncludeProcessor(Map<String, Object> config) {
        super(config);
    }

    public abstract boolean handles(String target);
    public abstract void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes);
    
}
