package org.asciidoctor.integrationguide.extension;

//tag::include[]
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class CopyrightFooterPostprocessor extends Postprocessor {    // <1>

    static final String COPYRIGHT_NOTICE = "Copyright Acme, Inc.";

    @Override
    public String process(Document document, String output) {

        org.jsoup.nodes.Document doc = Jsoup.parse(output, "UTF-8"); // <2>

        Element contentElement = doc.getElementById("footer-text");  // <3>
        if (contentElement != null) {
            contentElement.text(contentElement.ownText() + " | " + COPYRIGHT_NOTICE);
        }
        output = doc.html();                                         // <4>

        return output;
    }
}
//end::include[]
