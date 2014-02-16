package org.asciidoctor.extension;

import java.util.Map;

public abstract class BlockMacroProcessor extends MacroProcessor {

    public BlockMacroProcessor(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }
    
}
