package org.asciidoctor.integrationguide.syntaxhighlighter.threeparams;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;

import java.util.Map;

import static org.junit.Assert.assertEquals;


public class HighlightJsHighlighter implements SyntaxHighlighterAdapter {

//tag::include[]
    public HighlightJsHighlighter(String name, String backend, Map<String, Object> options) {
        assertEquals("myhighlightjs", name);
        assertEquals("html5", backend);
        Document document = (Document) options.get("document");
        assertEquals("Syntax Highlighter Test", document.getDoctitle());
    }
//end::include[]

    @Override
    public boolean hasDocInfo(LocationType location) {
        return location == LocationType.FOOTER;
    }

    @Override
    public String getDocinfo(LocationType location, Document document, Map<String, Object> options) { // <3>
        return "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/styles/github.min.css\">\n" +
            "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/highlight.min.js\"></script>\n" +
            "<script>hljs.initHighlighting()</script>";
    }

}
