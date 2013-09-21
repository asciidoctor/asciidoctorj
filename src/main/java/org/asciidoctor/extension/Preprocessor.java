package org.asciidoctor.extension;

import java.util.List;

import org.asciidoctor.internal.Document;
import org.asciidoctor.internal.PreprocessorReader;

public abstract class Preprocessor extends Processor {

    public Preprocessor(Document document) {
        super(document);
    }

    public abstract PreprocessorReader process(PreprocessorReader reader, List<String> lines);
    
}
