package org.asciidoctor.extension;

import java.util.Arrays;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;

public class ManpageMacro extends InlineMacroProcessor {

    public ManpageMacro(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }

    @Override
    protected String process(AbstractBlock parent, String target,
            Map<String, Object> attributes) {
        
        return createInline(parent, "anchor", Arrays.asList(target), attributes, getConfig()).render();
    }

}
