package org.asciidoctor.extension;

import org.asciidoctor.ast.StructuralNode;

import java.util.HashMap;
import java.util.Map;

public abstract class BlockMacroProcessor extends MacroProcessor<StructuralNode> {

    public BlockMacroProcessor() {
        this(null, new HashMap<>());
    }

    public BlockMacroProcessor(String macroName) {
        this(macroName, new HashMap<>());
    }

    public BlockMacroProcessor(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }
}
