package org.asciidoctor.extension;

import java.util.*;

import org.asciidoctor.ast.AbstractBlock;

public class YellStaticBlock extends BlockProcessor {

	private static Map<String, Object> configs = new HashMap<String, Object>() {{
		put("contexts", Arrays.asList(":paragraph"));
        put("content_model", ":simple");
	}};
	
    public YellStaticBlock(String name, Map<String, Object> config) {
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
