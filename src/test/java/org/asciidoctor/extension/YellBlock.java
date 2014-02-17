package org.asciidoctor.extension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;

public class YellBlock extends BlockProcessor {

    /**
     * This static block emulates the following DSL directives in the Ruby code:
     * 
     * option :contexts, [:paragraph]
     * option :content_model, :simple
     */
    /*static {
        config.put("contexts", Arrays.asList(":paragraph"));
        config.put("content_model", ":simple");
    }*/
  
    public YellBlock(String name, Map<String, Object> config) {
        super(name, config);
    }

    @Override
    public Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
        // FIXME this should be reader.read_lines()
        List<String> lines = reader.lines();
        String upperLines = null;
        for (String line : lines) {
            if (upperLines == null) {
                upperLines = line.toUpperCase();
            }
            else {
                upperLines = upperLines + "\n" + line.toUpperCase();
            }
        }

		return createBlock(parent, "paragraph", Arrays.asList(upperLines), attributes, new HashMap<String, Object>());
    }

}
