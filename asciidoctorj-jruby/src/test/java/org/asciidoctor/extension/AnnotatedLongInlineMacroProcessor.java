package org.asciidoctor.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;

@Name("man")
@Format(FormatType.LONG)
@PositionalAttributes({"section", "subsection"})
public class AnnotatedLongInlineMacroProcessor extends InlineMacroProcessor {

    public static final String SUBSECTION = "subsection";
    public static final String SECTION = "section";

    private final List<Map<String, Object>> receivedAttributes = new ArrayList<>();

    public List<Map<String, Object>> getReceivedAttributes() {
        return receivedAttributes;
    }

    @Override
    public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
        receivedAttributes.add(new HashMap<>(attributes));

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("type", ":link");
        options.put("target", target + ".html");
        return createPhraseNode(parent, "anchor", target, attributes, options);
    }
}
