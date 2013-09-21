package org.asciidoctor.extension;

import java.util.Map;

import org.asciidoctor.internal.Document;
import org.asciidoctor.internal.PreprocessorReader;

public abstract class IncludeProcessor extends Processor {

    public IncludeProcessor(Document document) {
        super(document);
    }

    public abstract boolean handles(String target);
    public abstract void process(PreprocessorReader reader, String target, Map<String, Object> attributes);
    
}
