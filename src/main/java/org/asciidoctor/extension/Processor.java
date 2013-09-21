package org.asciidoctor.extension;

import org.asciidoctor.internal.Document;

public class Processor {

    protected Document document;

    public Processor(Document document) {
        this.document = document;
    }

}
