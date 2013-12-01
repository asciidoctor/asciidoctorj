package org.asciidoctor.extension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.internal.Document;
import org.asciidoctor.internal.DocumentRuby;

public class ManpageMacro extends InlineMacroProcessor {

    public ManpageMacro(String macroName, DocumentRuby documentRuby) {
        super(macroName, documentRuby);
    }

    @Override
    protected String process(Document parent, String target,
            Map<String, Object> attributes) {
        
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("type", ":link");
        options.put("target", target+".html");
        
        return createInline(parent, "anchor", Arrays.asList(target), attributes, options).render();
    }

}
