package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//tag::include[]
@Name("ctrl")
public class KeyboardInlineMacroProcessor extends InlineMacroProcessor {

    @Override
    public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("keys", Arrays.asList("Ctrl", target));             // <1>
        return createPhraseNode(parent, "kbd", (String) null, attrs); // <2>
    }
}
//end::include[]
