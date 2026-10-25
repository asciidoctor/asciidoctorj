package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;
//tag::include[]
import org.asciidoctor.syntaxhighlighter.Formatter;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;

import java.util.Map;

public class HighlightJsWithLanguageHighlighter implements SyntaxHighlighterAdapter, Formatter { // <1>

//end::include[]
    /*
//tag::include[]
    // Methods hasDocInfo() and getDocInfo()
//end::include[]
    */
    @Override
    public boolean hasDocInfo(LocationType location) {
        return location == LocationType.FOOTER;
    }

    @Override
    public String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
        return "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/styles/github.min.css\">\n" +
            "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/highlight.min.js\"></script>\n" +
            "<script>hljs.initHighlighting()</script>";
    }
//tag::include[]

    @Override
    public String format(Block node, String lang, Map<String, Object> opts) {
        return "<pre class='highlight'><code class='" + lang + "'>" // <2>
            + node.getContent()                                     // <3>
            + "</code></pre>";
    }
}
//end::include[]