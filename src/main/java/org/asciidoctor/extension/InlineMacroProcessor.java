package org.asciidoctor.extension;

import org.asciidoctor.internal.DocumentRuby;

public abstract class InlineMacroProcessor extends MacroProcessor {

    public InlineMacroProcessor(DocumentRuby documentRuby, String macroName) {
        super(documentRuby, macroName);
    }

}
