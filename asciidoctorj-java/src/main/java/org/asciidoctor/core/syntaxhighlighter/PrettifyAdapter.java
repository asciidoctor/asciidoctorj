package org.asciidoctor.core.syntaxhighlighter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.Document;

/**
 * A syntax highlighter adapter for prettify, which performs syntax highlighting on the client.
 */
public class PrettifyAdapter extends AbstractSyntaxHighlighter {

    public PrettifyAdapter(String name, String backend, Map<String, Object> opts) {
        super(name, backend, opts);
        this.preClass = "prettyprint";
    }

    @Override
    public String format(AbstractBlock node, String lang, Map<String, Object> opts) {
        if (node.isOption("linenums")) {
            Map<String, Object> newOpts = new LinkedHashMap<>(opts);
            Object start = node.getAttribute("start");
            BiConsumer<Map<String, String>, Map<String, String>> transform = (pre, code) -> pre.put("class",
                    pre.get("class") + " " + (start != null ? "linenums:" + start : "linenums"));
            newOpts.put("transform", transform);
            opts = newOpts;
        }
        return super.format(node, lang, opts);
    }

    @Override
    public boolean hasDocinfo(String location) {
        return true;
    }

    @Override
    public String getDocinfo(String location, Document doc, Map<String, Object> opts) {
        Object baseUrl = doc.getAttribute("prettifydir", opts.get("cdn_base_url") + "/prettify/r298");
        if (location.equals("head")) {
            String prettifyTheme = doc.getStringAttribute("prettify-theme", "prettify");
            String prettifyThemeUrl = prettifyTheme.startsWith("http://") || prettifyTheme.startsWith("https://")
                    ? prettifyTheme : baseUrl + "/" + prettifyTheme + ".min.css";
            return "<link rel=\"stylesheet\" href=\"" + prettifyThemeUrl + "\""
                    + opts.getOrDefault("self_closing_tag_slash", "") + ">";
        }
        // footer
        return "<script src=\"" + baseUrl + "/run_prettify.min.js\"></script>";
    }
}
