package org.asciidoctor.integrationguide.extension;

//tag::include[]
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.Map;

@Name("gist")                                                          // <1>
public class GistBlockMacroProcessor extends BlockMacroProcessor {     // <2>

    @Override
    public Object process(                                             // <3>
            StructuralNode parent, String target, Map<String, Object> attributes) {

        String content = new StringBuilder()
            .append("<div class=\"openblock gist\">")
            .append("<div class=\"content\">")
            .append("<script src=\"https://gist.github.com/")
                .append(target)                                        // <4>
                .append(".js\"></script>")
            .append("</div>")
            .append("</div>").toString();

        return createBlock(parent, "pass", content);                   // <5>
    }

}
//end::include[]
