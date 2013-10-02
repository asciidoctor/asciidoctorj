package org.asciidoctor.extension;

import java.util.Map;

import org.asciidoctor.internal.Block;
import org.asciidoctor.internal.Document;
import org.asciidoctor.internal.DocumentRuby;

public class ManpageMacro extends InlineMacroProcessor {

    public ManpageMacro(String macroName, DocumentRuby documentRuby) {
        super(macroName, documentRuby);
    }

    @Override
    protected String process(Document parent, String target,
            Map<String, Object> attributes) {
        
        return "<a href=\"" + target + ".html\">" + target + "</a>";
    }

}
