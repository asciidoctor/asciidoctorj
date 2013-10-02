package org.asciidoctor.extension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.asciidoctor.internal.Block;
import org.asciidoctor.internal.DocumentRuby;
import org.asciidoctor.internal.Reader;

public class YellBlock extends BlockProcessor {

    /**
     * This static block emulates the following DSL directives in the Ruby code:
     * 
     * option :contexts, [:paragraph]
     * option :content_model, :simple
     */
    static {
        config.put("contexts", Arrays.asList(":paragraph"));
        config.put("content_model", ":simple");
    }
  
    // TODO accept instance-scoped options (i.e., additional config)
    public YellBlock(String context, DocumentRuby documentRuby) {
        super(context, documentRuby);
    }

    @Override
    public Object process(Block parent, Reader reader, Map<String, Object> attributes) {
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
        
        return createBlock(document, "paragraph", upperLines, attributes, new HashMap<String, Object>());
    }

}
