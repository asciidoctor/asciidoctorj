package org.asciidoctor.extension;

import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.StructuralNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YellStaticListingBlock extends BlockProcessor {

    private static Map<String, Object> configs = Map.of(
            Contexts.KEY, Arrays.asList(Contexts.LISTING),
            ContentModel.KEY, ContentModel.SIMPLE
    );

    public YellStaticListingBlock(String name) {
        super(name, configs);
    }

    public YellStaticListingBlock(String name, Map<String, Object> config) {
        super(name, configs);
    }

    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        List<String> lines = reader.readLines();
        String upperLines = null;
        for (String line : lines) {
            if (upperLines == null) {
                upperLines = line.toUpperCase();
            } else {
                upperLines = upperLines + "\n" + line.toUpperCase();
            }
        }

        return createBlock(parent, "paragraph", Arrays.asList(upperLines), attributes, new HashMap<>());
    }

}
