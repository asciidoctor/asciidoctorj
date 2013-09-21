package org.asciidoctor.extension;

import org.asciidoctor.internal.Document;

public abstract class Postprocessor extends Processor {

    public Postprocessor(Document document) {
        super(document);
    }

    public abstract String process(String output);
    
}
