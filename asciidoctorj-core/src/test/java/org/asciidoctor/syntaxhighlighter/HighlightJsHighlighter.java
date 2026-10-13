package org.asciidoctor.syntaxhighlighter;

import java.util.Map;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;

public class HighlightJsHighlighter implements SyntaxHighlighterAdapter, Formatter {

    static String DOCINFO_HEADER = "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/styles/github.min.css\">";
    static String DOCINFO_FOOTER = "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/highlight.min.js\"></script>\n"
        + "<script>hljs.initHighlighting()</script>";

    @Override
    public boolean hasDocInfo(LocationType location) {
        return true;
    }

    @Override
    public String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
        return location == LocationType.HEADER ? DOCINFO_HEADER : DOCINFO_FOOTER;
    }

    @Override
    public String format(Block node, String lang, Map<String, Object> opts) {
        return "<pre class=\"highlightjs highlight\"><code data-lang=\"" + lang + "\" class=\"language-" + lang + " hljs\">" + node.getContent() + "</code></pre>";
    }
}
