package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//tag::include[]
@Name("rightclick")
public class ContextMenuInlineMacroProcessor extends InlineMacroProcessor {

    @Override
    public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
        String[] items = target.split("\\|");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("menu", "Right click");                              // <1>
        List<String> submenus = Arrays.asList(items).subList(0, items.length - 1);
        attrs.put("submenus", submenus);
        attrs.put("menuitem", items[items.length - 1]);

        return createPhraseNode(parent, "menu", (String) null, attrs); // <2>
    }
}
//end::include[]
