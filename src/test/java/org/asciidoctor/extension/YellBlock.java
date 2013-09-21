package org.asciidoctor.extension;

import org.asciidoctor.internal.DocumentRuby;

public class YellBlock extends BlockProcessor {

    public YellBlock(DocumentRuby documentRuby) {
        super(documentRuby);
    }

    @Override
    public Object process(Object parent, Object reader, Object attributes) {
        
        System.out.println("block");
        
        return null;
    }

}
