package org.asciidoctor.extension;

import org.asciidoctor.internal.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class CustomFooterPostProcessor extends Postprocessor {

    public CustomFooterPostProcessor(Document document) {
        super(document);
    }

    @Override
    public String process(String output) {
        
        String copyright  = "Copyright Acme, Inc.";
        
        if(this.document.basebackend("html")) {
            org.jsoup.nodes.Document doc = Jsoup.parse(output, "UTF-8");

            Element contentElement = doc.getElementById("footer-text");
            contentElement.append(copyright);
            
            output = doc.html();
            
        }

        
        return output;
    }

}
