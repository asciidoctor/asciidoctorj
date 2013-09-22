package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.internal.Block;
import org.asciidoctor.internal.Document;
import org.asciidoctor.internal.DocumentRuby;

public abstract class BlockMacroProcessor extends MacroProcessor {

    protected String macroName;
    
    public BlockMacroProcessor(DocumentRuby documentRuby, String macroName) {
        super(documentRuby);
        this.macroName = macroName;
    }

    public Map<Object, Object> options() {
        return new HashMap<Object, Object>();
    }
    
    public Block process(DocumentRuby parent, String target, Map<String, Object> attributes) {
        return process(new Document(parent, rubyRuntime), target, attributes);
    }
    
    protected abstract Block process(Document parent, String target, Map<String, Object> attributes);
    
}
