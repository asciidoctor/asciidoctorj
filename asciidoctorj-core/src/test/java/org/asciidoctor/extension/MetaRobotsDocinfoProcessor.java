package org.asciidoctor.extension;

import org.asciidoctor.ast.DocumentRuby;

import java.util.Map;

public class MetaRobotsDocinfoProcessor extends DocinfoProcessor {

    public MetaRobotsDocinfoProcessor(Map<String, Object> config) {
        super(config);
    }

    @Override
    public String process(DocumentRuby document) {
        return "<meta name=\"robots\" content=\"index,follow\">";
    }
}
