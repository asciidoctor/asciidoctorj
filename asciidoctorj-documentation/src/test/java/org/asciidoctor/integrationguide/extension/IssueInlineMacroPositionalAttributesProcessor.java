package org.asciidoctor.integrationguide.extension;

//tag::include[]
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.PositionalAttributes;

//end::include[]
import java.util.HashMap;
import java.util.Map;

//tag::include[]
@Name("issue")
@PositionalAttributes({"repo"})     // <1>
public class IssueInlineMacroPositionalAttributesProcessor extends InlineMacroProcessor {

    @Override
    public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {

        String href =
                new StringBuilder()
                    .append("https://github.com/")
                    .append(attributes.containsKey("repo") ?
                            attributes.get("repo") :
                            parent.getDocument().getAttribute("repo"))
                    .append("/issues/")
                    .append(target).toString();

        Map<String, Object> options = new HashMap<>();
        options.put("type", ":link");
        options.put("target", href);
        return createPhraseNode(parent, "anchor", target, attributes, options); // <4>
    }

}
//end::include[]
