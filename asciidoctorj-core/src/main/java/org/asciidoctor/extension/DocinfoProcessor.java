package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import java.util.HashMap;
import java.util.Map;

public abstract class DocinfoProcessor extends Processor {

    public DocinfoProcessor() {
        super(new HashMap<>());
    }

    public DocinfoProcessor(Map<String, Object> config) {
        super(config);
    }

    public abstract String process(Document document);

}
