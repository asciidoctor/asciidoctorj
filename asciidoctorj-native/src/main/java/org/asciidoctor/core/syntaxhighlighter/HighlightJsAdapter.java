package org.asciidoctor.core.syntaxhighlighter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.Asciidoctor;
import org.asciidoctor.core.Document;
import org.asciidoctor.core.Strings;

/**
 * A syntax highlighter adapter for highlight.js, which performs syntax highlighting on the client.
 */
public class HighlightJsAdapter extends AbstractSyntaxHighlighter {

    public HighlightJsAdapter(String name, String backend, Map<String, Object> opts) {
        super(name, backend, opts);
        this.name = this.preClass = "highlightjs";
    }

    @Override
    public String format(AbstractBlock node, String lang, Map<String, Object> opts) {
        Map<String, Object> newOpts = new LinkedHashMap<>(opts);
        BiConsumer<Map<String, String>, Map<String, String>> transform = (pre, code) -> {
            if (node.hasAttribute("nohighlight-option")) {
                pre.put("class", pre.get("class").replaceFirst(" highlight", ""));
            }
            code.put("class", "language-" + (lang == null ? "none" : lang) + " hljs");
        };
        newOpts.put("transform", transform);
        return super.format(node, lang, newOpts);
    }

    @Override
    public boolean hasDocinfo(String location) {
        return true;
    }

    @Override
    public String getDocinfo(String location, Document doc, Map<String, Object> opts) {
        Object baseUrl = doc.getAttribute("highlightjsdir", opts.get("cdn_base_url") + "/highlight.js/"
                + Asciidoctor.HIGHLIGHT_JS_VERSION);
        if (location.equals("head")) {
            return "<link rel=\"stylesheet\" href=\"" + baseUrl + "/styles/" + doc.getAttribute("highlightjs-theme",
                    "github") + ".min.css\"" + opts.getOrDefault("self_closing_tag_slash", "") + ">";
        }
        // footer
        StringBuilder languages = new StringBuilder();
        if (doc.hasAttribute("highlightjs-languages")) {
            for (String lang : Strings.split(doc.getStringAttribute("highlightjs-languages"), ",")) {
                languages.append("<script src=\"").append(baseUrl).append("/languages/").append(Strings.lstrip(lang))
                        .append(".min.js\"></script>\n");
            }
        }
        return "<script src=\"" + baseUrl + "/highlight.min.js\"></script>\n"
                + languages + "<script>\n"
                + "if (!hljs.initHighlighting.called) {\n"
                + "  hljs.initHighlighting.called = true\n"
                + "  ;[].slice.call(document.querySelectorAll('pre.highlight > code[data-lang]')).forEach(function (el)"
                + " { hljs.highlightBlock(el) })\n"
                + "}\n"
                + "</script>";
    }
}
