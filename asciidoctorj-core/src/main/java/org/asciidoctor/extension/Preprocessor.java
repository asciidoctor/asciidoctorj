package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.Document;

public abstract class Preprocessor extends Processor {

    public Preprocessor() {
        this(new HashMap<String, Object>());
    }
    
    public Preprocessor(Map<String, Object> config) {
        super(config);
    }

    public abstract PreprocessorReader process(Document document, PreprocessorReader reader);

}
