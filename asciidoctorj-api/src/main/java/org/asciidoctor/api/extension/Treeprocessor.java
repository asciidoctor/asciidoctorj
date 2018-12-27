package org.asciidoctor.api.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.api.ast.Document;

public abstract class Treeprocessor extends BaseProcessor {

    public Treeprocessor() {
        this(new HashMap<>());
    }

    public Treeprocessor(Map<String, Object> config) {
        super(config);
    }

    public abstract Document process(Document document);
    
}
