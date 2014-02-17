package org.asciidoctor.extension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;

public class GistMacro extends BlockMacroProcessor {

    public GistMacro(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }
    
    @Override
    public Block process(AbstractBlock parent, String target, Map<String, Object> attributes) {
       
       String content = "<div class=\"content\">\n" + 
       		"<script src=\"https://gist.github.com/"+target+".js\"></script>\n" + 
       		"</div>"; 
       
       Map<String, Object> options = new HashMap<String, Object>() {{
           put("content_model", ":raw");
       }
       };
       
       return createBlock(parent, "pass", Arrays.asList(content), attributes, options);
    }

}
