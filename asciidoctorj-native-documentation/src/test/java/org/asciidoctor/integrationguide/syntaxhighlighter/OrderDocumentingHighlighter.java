package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;
import org.asciidoctor.syntaxhighlighter.Formatter;
import org.asciidoctor.syntaxhighlighter.HighlightResult;
import org.asciidoctor.syntaxhighlighter.Highlighter;
import org.asciidoctor.syntaxhighlighter.StylesheetWriter;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderDocumentingHighlighter implements SyntaxHighlighterAdapter, Formatter, StylesheetWriter, Highlighter {

    public static List<String> messages = new ArrayList<>();

    public OrderDocumentingHighlighter() {
        messages.add("New SyntaxHighlighter");
    }

    @Override
    public boolean hasDocInfo(LocationType location) {
        messages.add("hasDocInfo for " + location);
        return true;
    }

    @Override
    public String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
        messages.add("getDocInfo for " + location);
        return "";
    }

    @Override
    public String format(Block node, String lang, Map<String, Object> opts) {
        messages.add("format `" + lang + "`");
        return "<pre class='highlight'><code>"
            + node.getContent()
            + "</code></pre>";
    }

    @Override
    public boolean isWriteStylesheet(Document doc) {
        messages.add("isWriteStylesheet");
        return doc.hasAttribute("linkcss") && doc.hasAttribute("copycss");
    }

    @Override
    public void writeStylesheet(Document doc, File toDir) {
        messages.add("writeStylesheet");
    }

    @Override
    public HighlightResult highlight(Block node,
                                     String source,
                                     String lang,
                                     Map<String, Object> options) {
        messages.add("highlight `" + source + "`");
        return new HighlightResult(source);
    }
}