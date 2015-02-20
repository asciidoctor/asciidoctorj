package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;

public abstract class MacroProcessor extends Processor {

    protected String name;
    
    public MacroProcessor(String name) {
        this(name, new HashMap<String, Object>());
    }
    
    public MacroProcessor(String name, Map<String, Object> config) {
        super(config);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Object, Object> options() {
        return new HashMap<Object, Object>();
    }
    
    public abstract Object process(AbstractBlock parent, String target, Map<String, Object> attributes);
    
}
