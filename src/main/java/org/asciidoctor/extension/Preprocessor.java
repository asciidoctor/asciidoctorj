package org.asciidoctor.extension;

import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;

public abstract class Preprocessor extends Processor {

    public Preprocessor(Map<String, Object> config) {
        super(config);
    }

    public abstract PreprocessorReader process(Document document, PreprocessorReader reader);
    
    public PreprocessorReader process(DocumentRuby document, PreprocessorReader reader) {
    	return this.process(document(document), reader);
    }
    
}
