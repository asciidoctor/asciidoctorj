package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.ast.Document;
//tag::include[]
import org.asciidoctor.extension.LocationType;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;

import java.util.Map;

public class HighlightJsHighlighter implements SyntaxHighlighterAdapter { // <1>

    @Override
    public boolean hasDocInfo(LocationType location) {
        return location == LocationType.FOOTER;         // <2>
    }

    @Override
    public String getDocinfo(LocationType location, Document document, Map<String, Object> options) { // <3>
        return "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/styles/github.min.css\">\n" +
            "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/highlight.min.js\"></script>\n" +
            "<script>hljs.initHighlighting()</script>";
    }

}
//end::include[]