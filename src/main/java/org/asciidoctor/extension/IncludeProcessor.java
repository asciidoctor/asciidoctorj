package org.asciidoctor.extension;

import java.util.Map;

public abstract class IncludeProcessor extends Processor {

    public IncludeProcessor(Map<String, Object> config) {
        super(config);
    }

    public abstract boolean handles(String target);
    public abstract void process(PreprocessorReader reader, String target, Map<String, Object> attributes);
    
}
