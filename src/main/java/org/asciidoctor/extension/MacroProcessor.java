package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.internal.Document;
import org.asciidoctor.internal.DocumentRuby;

public abstract class MacroProcessor extends Processor {

    protected String macroName;
    
    public MacroProcessor(String macroName, DocumentRuby documentRuby) {
        super(documentRuby);
        this.macroName = macroName;
    }


    public Map<Object, Object> options() {
        return new HashMap<Object, Object>();
    }
    
    public Object process(DocumentRuby parent, String target, Map<String, Object> attributes) {
        return process(new Document(parent, rubyRuntime), target, attributes);
    }
    
    protected abstract Object process(Document parent, String target, Map<String, Object> attributes);
    
}
