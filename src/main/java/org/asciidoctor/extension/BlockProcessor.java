package org.asciidoctor.extension;

import org.asciidoctor.internal.DocumentRuby;

public abstract class BlockProcessor extends Processor {

    public BlockProcessor(DocumentRuby documentRuby) {
        super(documentRuby);
    }

    public void config() {
        
    }
    
    public abstract Object process(Object parent, Object reader, Object attributes);
    
}
