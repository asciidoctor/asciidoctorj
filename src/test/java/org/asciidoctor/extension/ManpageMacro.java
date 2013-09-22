package org.asciidoctor.extension;

import java.util.Map;

import org.asciidoctor.internal.Block;
import org.asciidoctor.internal.Document;
import org.asciidoctor.internal.DocumentRuby;

public class ManpageMacro extends InlineMacroProcessor {

    public ManpageMacro(DocumentRuby documentRuby, String macroName) {
        super(documentRuby, macroName);
    }

    @Override
    protected Block process(Document parent, String target,
            Map<String, Object> attributes) {
        
        System.out.println("Hello");
        
        return null;
    }

}
