package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.StructuralNode;

public class ConfigModifyingBlockProcessor extends BlockProcessor {

    public ConfigModifyingBlockProcessor() {
        super("modify");
    }

    public ConfigModifyingBlockProcessor(String name) {
        super(name);

        Map<String, Object> config = new HashMap<>();
        config.put(Contexts.KEY, List.of(Contexts.PARAGRAPH));
        config.put(ContentModel.KEY, ContentModel.SIMPLE);
        setConfig(config);
    }

    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        Map<String, Object> config = new HashMap<>();
        setConfig(config);
        return config;
    }
}
