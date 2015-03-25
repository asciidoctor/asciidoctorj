package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.5.2
 */
public abstract class DocinfoProcessor extends Processor {

    public DocinfoProcessor() {
        super(defaultLocation(new HashMap<String, Object>()));
    }

    public DocinfoProcessor(Map<String, Object> config) {
        super(defaultLocation(config));
    }

    public abstract String process(Document document);

    public String process(DocumentRuby documentRuby) {
        return this.process(document(documentRuby));
    }

    private static final Map<String, Object> defaultLocation(Map<String, Object> map) {
        if(!map.containsKey("location")) {
            map.put("location", ":header");
        }
        return map;
    }
}
