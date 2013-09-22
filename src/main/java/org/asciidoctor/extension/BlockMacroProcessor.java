package org.asciidoctor.extension;

import org.asciidoctor.internal.DocumentRuby;

public abstract class BlockMacroProcessor extends MacroProcessor {

    public BlockMacroProcessor(DocumentRuby documentRuby, String macroName) {
        super(documentRuby, macroName);
    }
    
}
