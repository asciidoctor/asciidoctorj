package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.StructuralNode;

@Name("list")
public class ListCreatorBlockMacro extends BlockMacroProcessor {

    String context;
    boolean attributesInOptions;

    public ListCreatorBlockMacro(String context) {
        this(context, false);
    }

    public ListCreatorBlockMacro(String context, boolean attributesInOptions) {
        this.context = context;
        this.attributesInOptions = attributesInOptions;
    }

    @Override
    public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {

        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("start", "42");
        Map<Object, Object> opts = new HashMap<Object, Object>();

        org.asciidoctor.ast.List list;
        if (attributesInOptions) {
            opts.put("attributes", attrs);
            list = createList(parent, context, opts);
        } else {
            list = createList(parent, context, attrs, opts);
        }

        list.getBlocks().add(createListItem(list, "First item"));
        list.getBlocks().add(createListItem(list, "Second item"));

        return list;
    }
}
