package org.asciidoctor.extension;

import org.asciidoctor.dom.DocumentRuby;

public abstract class Postprocessor extends Processor {

    public Postprocessor(DocumentRuby documentRuby) {
        super(documentRuby);
    }

    public abstract String process(String output);
    
}
