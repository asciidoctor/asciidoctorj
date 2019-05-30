package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//tag::include[]
@Name("ctrl")
public class KeyboardInlineMacroProcessor extends InlineMacroProcessor {

    @Override
    public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("keys", Arrays.asList("Ctrl", target));             // <1>
        return createPhraseNode(parent, "kbd", (String) null, attrs); // <2>
    }
}
//end::include[]
