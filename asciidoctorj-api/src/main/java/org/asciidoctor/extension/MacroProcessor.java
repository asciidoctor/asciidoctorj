package org.asciidoctor.extension;

import org.asciidoctor.ast.ContentNode;

import java.util.HashMap;
import java.util.Map;

public abstract class MacroProcessor<T extends ContentNode> extends BaseProcessor {

    protected String name;

    public MacroProcessor(String name) {
        this(name, new HashMap<>());
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
        return new HashMap<>();
    }
    
    public abstract Object process(T parent, String target, Map<String, Object> attributes);
    
}
