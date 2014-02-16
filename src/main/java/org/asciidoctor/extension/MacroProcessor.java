package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;

public abstract class MacroProcessor extends Processor {

    protected String macroName;
    
    public MacroProcessor(String macroName, Map<String, Object> config) {
        super(config);
        this.macroName = macroName;
    }


    public Map<Object, Object> options() {
        return new HashMap<Object, Object>();
    }
    
    public Object process(DocumentRuby parent, String target, Map<String, Object> attributes) {
        return process(document(parent), target, attributes);
    }
    
    protected abstract Object process(Document parent, String target, Map<String, Object> attributes);
    
}
