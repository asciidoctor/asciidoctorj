package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.Map;

public class CustomFooterPostProcessor extends Postprocessor {

    public CustomFooterPostProcessor(Map<String, Object> config) {
        super(config);
    }

    @Override
    public String process(Document document, String output) {
        
        String copyright  = "Copyright Acme, Inc.";
        
        if(document.isBasebackend("html")) {
            org.jsoup.nodes.Document doc = Jsoup.parse(output, "UTF-8");

            Element contentElement = doc.getElementById("footer-text");
            contentElement.append(copyright);
            
            output = doc.html();
            
        }

        
        return output;
    }

}
