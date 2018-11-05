package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.Document;

public abstract class Preprocessor extends BaseProcessor {

    public Preprocessor() {
        this(new HashMap<>());
    }

    public Preprocessor(Map<String, Object> config) {
        super(config);
    }

    public abstract void process(Document document, PreprocessorReader reader);

}
