package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;

@Name("man")
@Format(FormatType.LONG)
@PositionalAttributes({"section", "subsection"})
public class AnnotatedLongInlineMacroProcessor extends InlineMacroProcessor {

    public static final String SUBSECTION = "subsection";
    public static final String SECTION = "section";

    @Override
    public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
        assert "7".equals(attributes.get(SECTION)) || ("8".equals(attributes.get(SECTION)) || "1".equals(attributes.get(SUBSECTION)));

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("type", ":link");
        options.put("target", target + ".html");
        return createPhraseNode(parent, "anchor", target, attributes, options);
    }
}
