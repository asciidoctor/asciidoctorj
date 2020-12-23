package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//tag::include[]
@Name("rightclick")
public class ContextMenuInlineMacroProcessor extends InlineMacroProcessor {

    @Override
    public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
        String[] items = target.split("\\|");
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("menu", "Right click");                              // <1>
        List<String> submenus = new ArrayList<String>();
        for (int i = 0; i < items.length - 1; i++) {
            submenus.add(items[i]);
        }
        attrs.put("submenus", submenus);
        attrs.put("menuitem", items[items.length - 1]);

        return createPhraseNode(parent, "menu", (String) null, attrs); // <2>
    }
}
//end::include[]
