package org.asciidoctor.extension;

import org.asciidoctor.internal.Document;

public class YellBlock extends BlockProcessor {

    public YellBlock(Document document) {
        super(document);
    }

    @Override
    public Object process(Object parent, Object reader, Object attributes) {
        
        System.out.println("block");
        
        return null;
    }

}
