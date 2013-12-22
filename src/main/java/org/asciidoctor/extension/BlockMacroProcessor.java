package org.asciidoctor.extension;

import org.asciidoctor.dom.DocumentRuby;

public abstract class BlockMacroProcessor extends MacroProcessor {

    public BlockMacroProcessor(String macroName, DocumentRuby documentRuby) {
        super(macroName, documentRuby);
    }
    
}
