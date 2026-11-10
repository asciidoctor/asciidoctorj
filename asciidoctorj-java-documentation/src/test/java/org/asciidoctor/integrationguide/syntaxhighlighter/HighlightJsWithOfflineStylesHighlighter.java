package org.asciidoctor.integrationguide.syntaxhighlighter;

import org.apache.commons.io.IOUtils;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;
import org.asciidoctor.syntaxhighlighter.Formatter;
import org.asciidoctor.syntaxhighlighter.StylesheetWriter;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

//tag::include[]

public class HighlightJsWithOfflineStylesHighlighter implements SyntaxHighlighterAdapter, Formatter, StylesheetWriter { // <1>

    @Override
    public boolean hasDocInfo(LocationType location) {
        return location == LocationType.FOOTER;
    }

    @Override
    public String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
        if (document.hasAttribute("linkcss") && document.hasAttribute("copycss")) { // <2>
            return "<link rel=\"stylesheet\" href=\"github.min.css\">\n" +
                "<script src=\"highlight.min.js\"></script>\n" +
                "<script>hljs.initHighlighting()</script>";
        } else {
            return "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/styles/github.min.css\">\n" +
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/highlight.min.js\"></script>\n" +
                "<script>hljs.initHighlighting()</script>";
        }
    }

    @Override
    public String format(Block node, String lang, Map<String, Object> opts) {
        return "<pre class='highlight'><code class='" + lang + "'>"
            + node.getContent()
            + "</code></pre>";
    }

    @Override
    public boolean isWriteStylesheet(Document doc) {
        return true; // <3>
    }

    @Override
    public void writeStylesheet(Document doc, File toDir) {
        try {    // <4>
            URL url1 = new URL("https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/styles/github.min.css");
            URL url2 = new URL("https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/highlight.min.js");

            try (InputStream in1 = url1.openStream();
                 OutputStream fout1 = new FileOutputStream(new File(toDir, "github.min.css"))) {
                IOUtils.copy(in1, fout1);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }

            try (InputStream in2 = url2.openStream();
                 OutputStream fout2 = new FileOutputStream(new File(toDir, "highlight.min.js"))) {
                IOUtils.copy(in2, fout2);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
//end::include[]
