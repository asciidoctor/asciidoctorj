package org.asciidoctor.extension;

import java.util.Map;

import org.asciidoctor.ast.Document;
import org.jsoup.Jsoup;

public class CustomFooterPostProcessor extends Postprocessor {

    public CustomFooterPostProcessor(Map<String, Object> config) {
        super(config);
    }

    @Override
    public String process(Document document, String output) {
        
        String copyright  = "Copyright Acme, Inc.";
        
        if (document.basebackend("html")) {
            org.jsoup.nodes.Document doc = Jsoup.parse(output, "UTF-8");

            doc.getElementById("footer-text")
                    .append(copyright);
            
            return doc.html();
        }

        return output;
    }

}
