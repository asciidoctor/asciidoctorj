package org.asciidoctor.extension;

import org.asciidoctor.ast.AbstractBlock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YellStaticListingBlock extends BlockProcessor {

	private static Map<String, Object> configs = new HashMap<String, Object>() {{
		put("contexts", Arrays.asList(":listing"));
        put("content_model", ":simple");
	}};

    public YellStaticListingBlock(String name) {
        super(name, configs);
    }

    public YellStaticListingBlock(String name, Map<String, Object> config) {
        super(name, configs);
    }

    @Override
    public Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
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
