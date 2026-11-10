package org.asciidoctor.integrationguide.extension;

//tag::include[]
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.PositionalAttributes;

import java.util.Map;

@Name("gist")
@PositionalAttributes({"provider", "repo"})                  // <1>
public class GistBlockMacroPositionalAttributesProcessor extends BlockMacroProcessor {

    @Override
    public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {

        String script;
        String provider = (String) attributes.get("provider");
        if (provider == null || "github".equals(provider)) { // <2>
            script = String.format("<script src=\"https://gist.github.com/%s.js\"/></script>", target);
        } else if ("gitlab".equals(provider)) {
            String repo = (String) attributes.get("repo");
            if (repo == null) {
                script = String.format("<script src=\"https://gitlab.com/-/snippets/%s.js\"></script>", target);
            } else {
                script = String.format("<script src=\"https://gitlab.com/%s/-/snippets/%s.js\"></script>", repo, target);
            }
        } else {
            throw new IllegalArgumentException("Unknown provider " + provider);
        }

        String content = new StringBuilder()
            .append("<div class=\"openblock gist\">")
            .append("<div class=\"content\">")
            .append(script)
            .append("</div>")
            .append("</div>").toString();

        return createBlock(parent, "pass", content);
    }

}
//end::include[]
