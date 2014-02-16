package org.asciidoctor.extension;

import java.util.List;
import java.util.Map;

public abstract class Preprocessor extends Processor {

    public Preprocessor(Map<String, Object> config) {
        super(config);
    }

    public abstract PreprocessorReader process(PreprocessorReader reader, List<String> lines);
    
}
