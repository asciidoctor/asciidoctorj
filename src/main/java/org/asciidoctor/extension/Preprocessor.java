package org.asciidoctor.extension;

import java.util.List;

import org.asciidoctor.dom.DocumentRuby;

public abstract class Preprocessor extends Processor {

    public Preprocessor(DocumentRuby documentRuby) {
        super(documentRuby);
    }

    public abstract PreprocessorReader process(PreprocessorReader reader, List<String> lines);
    
}
