package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import java.util.HashMap;
import java.util.Map;

public abstract class DocinfoProcessor extends Processor {

    public DocinfoProcessor() {
        super(defaultLocation(new HashMap<String, Object>()));
    }

    public DocinfoProcessor(Map<String, Object> config) {
        super(defaultLocation(config));
    }

    public abstract String process(Document document);

    private static final Map<String, Object> defaultLocation(Map<String, Object> map) {
        if(!map.containsKey("location")) {
            map.put("location", ":header");
        }
        return map;
    }
}
