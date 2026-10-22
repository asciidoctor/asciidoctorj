package org.asciidoctor.core.syntaxhighlighter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.asciidoctor.core.AbstractBlock;

/**
 * The base class for syntax highlighter adapters that provides the default implementation of {@link #format}.
 */
public abstract class AbstractSyntaxHighlighter implements SyntaxHighlighter {
    protected String name;
    protected String preClass;

    protected AbstractSyntaxHighlighter(String name, String backend, Map<String, Object> opts) {
        this.name = name;
        this.preClass = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Formats the highlighted source wrapped in pre and code tags.
     *
     * <p>The opts may contain a transform entry, a {@link BiConsumer} that receives the Maps of attributes for the pre
     * and code tags so they can be modified.
     */
    @Override
    @SuppressWarnings("unchecked")
    public String format(AbstractBlock node, String lang, Map<String, Object> opts) {
        String classAttrVal = Boolean.TRUE.equals(opts.get("nowrap")) ? preClass + " highlight nowrap"
                : preClass + " highlight";
        Object transform = opts.get("transform");
        if (transform != null) {
            Map<String, String> pre = new LinkedHashMap<>();
            pre.put("class", classAttrVal);
            Map<String, String> code = new LinkedHashMap<>();
            if (lang != null) {
                code.put("data-lang", lang);
            }
            ((BiConsumer<Map<String, String>, Map<String, String>>) transform).accept(pre, code);
            // NOTE make sure data-lang is the last attribute on the code tag to remain consistent with 1.5.x
            String dataLang = code.remove("data-lang");
            if (dataLang != null) {
                code.put("data-lang", dataLang);
            }
            StringBuilder result = new StringBuilder("<pre");
            pre.forEach((k, v) -> result.append(' ').append(k).append("=\"").append(v).append('"'));
            result.append("><code");
            code.forEach((k, v) -> result.append(' ').append(k).append("=\"").append(v).append('"'));
            result.append('>').append(node.getContent()).append("</code></pre>");
            return result.toString();
        }
        return "<pre class=\"" + classAttrVal + "\"><code" + (lang != null ? " data-lang=\"" + lang + "\"" : "") + ">"
                + node.getContent() + "</code></pre>";
    }
}
