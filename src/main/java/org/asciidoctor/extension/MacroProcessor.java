package org.asciidoctor.extension;

import org.asciidoctor.internal.DocumentRuby;

public class MacroProcessor extends Processor {

    protected String macroName;
    
    public MacroProcessor(DocumentRuby documentRuby, String macroName) {
        super(documentRuby);
        this.macroName = macroName;
    }

}
