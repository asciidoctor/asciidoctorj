package org.asciidoctor.extension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.BlockNode;

public class YellStaticBlock extends BlockProcessor {

	private static Map<String, Object> configs = new HashMap<String, Object>() {{
		put(CONTEXTS, Arrays.asList(CONTEXT_PARAGRAPH));
        put(CONTENT_MODEL, CONTENT_MODEL_SIMPLE);
	}};

    public YellStaticBlock(String name) {
        super(name, configs);
    }

    public YellStaticBlock(String name, Map<String, Object> config) {
        super(name, configs);
    }

    @Override
    public Object process(BlockNode parent, Reader reader, Map<String, Object> attributes) {
        List<String> lines = reader.readLines();
        String upperLines = null;
        for (String line : lines) {
            if (upperLines == null) {
                upperLines = line.toUpperCase();
            }
            else {
                upperLines = upperLines + "\n" + line.toUpperCase();
            }
        }

		return createBlock(parent, "paragraph", Arrays.asList(upperLines), attributes, new HashMap<Object, Object>());
    }

}
