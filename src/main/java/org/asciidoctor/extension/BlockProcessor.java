package org.asciidoctor.extension;

import org.asciidoctor.internal.Document;

public abstract class BlockProcessor extends Processor {

    public BlockProcessor(Document document) {
        super(document);
    }

    public void config() {
        
    }
    
    public abstract Object process(Object parent, Object reader, Object attributes);
    
}
