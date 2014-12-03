package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;

public abstract class BlockProcessor extends Processor {

    protected String name;
    
    public BlockProcessor(String name) {
        this(name, new HashMap<String, Object>());
    }
    
    public BlockProcessor(String name, Map<String, Object> config) {
        super(config);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public abstract Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes);
}
