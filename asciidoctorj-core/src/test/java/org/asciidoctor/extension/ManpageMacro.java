package org.asciidoctor.extension;

import org.asciidoctor.ast.AbstractBlock;

import java.util.HashMap;
import java.util.Map;

public class ManpageMacro extends InlineMacroProcessor {

    public ManpageMacro(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }

    @Override
    protected String process(AbstractBlock parent, String target,
            Map<String, Object> attributes) {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("type", ":link");
        options.put("target", target + ".html");
        return createInline(parent, "anchor", target, attributes, options).convert();
    }

}
