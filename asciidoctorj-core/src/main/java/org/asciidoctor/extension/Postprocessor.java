package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;

public abstract class Postprocessor extends Processor {

    public Postprocessor() {
        this(new HashMap<String, Object>());
    }
    
    public Postprocessor(Map<String, Object> config) {
        super(config);
    }

    public abstract String process(Document document, String output);
    
    public String process(DocumentRuby documentRuby, String output) {
    	return process(document(documentRuby), output);
    }
    
}
