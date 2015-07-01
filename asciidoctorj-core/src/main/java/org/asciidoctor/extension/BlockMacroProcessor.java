package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

public abstract class BlockMacroProcessor extends MacroProcessor {

    public BlockMacroProcessor() {
        this(null, new HashMap<String, Object>());
    }

    public BlockMacroProcessor(String macroName) {
        this(macroName, new HashMap<String, Object>());
    }
    
    public BlockMacroProcessor(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }
    
}
