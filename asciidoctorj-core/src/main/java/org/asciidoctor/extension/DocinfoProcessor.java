package org.asciidoctor.extension;

import org.asciidoctor.ast.DocumentRuby;

import java.util.HashMap;
import java.util.Map;

public abstract class DocinfoProcessor extends Processor {

    public DocinfoProcessor() {
        super(new HashMap<String, Object>());
    }

    public DocinfoProcessor(Map<String, Object> config) {
        super(config);
    }

    public abstract String process(DocumentRuby document);

}
