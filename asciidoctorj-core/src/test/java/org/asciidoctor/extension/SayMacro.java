package org.asciidoctor.extension;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.PhraseNode;

import java.util.HashMap;
import java.util.Map;

public class SayMacro extends InlineMacroProcessor {

    public SayMacro(String macroName) {
        super(macroName);
    }

    public SayMacro(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }

    @Override
    public PhraseNode process(ContentNode parent, String target, Map<String, Object> attributes) {
        String text = "*" + target + "*";
        Map<String, Object> phraseNodeAttributes = new HashMap<>();
        phraseNodeAttributes.put("subs", ":normal");
        return createPhraseNode(parent, "quoted", text, phraseNodeAttributes);
    }

}
