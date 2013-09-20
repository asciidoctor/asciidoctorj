package org.asciidoctor.extension;

import java.util.List;

import org.asciidoctor.internal.Document;
import org.asciidoctor.internal.PreprocessorReader;

public abstract class Preprocessor {

    protected Document document;
    
    public Preprocessor(Document document) {
        this.document = document;
    }
    
    public abstract PreprocessorReader process(PreprocessorReader reader, List<String> lines);
    
}
