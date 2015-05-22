package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.Document;

public abstract class Treeprocessor extends Processor {

    public Treeprocessor() {
        this(new HashMap<String, Object>());
    }
    
    public Treeprocessor(Map<String, Object> config) {
        super(config);
    }

    public abstract Document process(Document document);
    
}
