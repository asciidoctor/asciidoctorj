package org.asciidoctor.extension;

import org.asciidoctor.ast.StructuralNode;

import java.util.HashMap;
import java.util.Map;

public abstract class BlockProcessor extends BaseProcessor {

    protected String name;

    public BlockProcessor() {
        this(null);
    }

    public BlockProcessor(String name) {
        this(name, new HashMap<>());
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

    public abstract Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes);
}
