package org.asciidoctor.extension;

import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;

import java.util.Map;

public class ManpageMacro extends InlineMacroProcessor {

    public ManpageMacro(String macroName) {
        super(macroName);
    }

    public ManpageMacro(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }

    @Override
    public PhraseNode process(StructuralNode parent, String target,
                              Map<String, Object> attributes) {
        Map<String, Object> options = Map.of(
                "type", ":link",
                "target", target + ".html");
        return createPhraseNode(parent, "anchor", target, attributes, options);
    }

}
