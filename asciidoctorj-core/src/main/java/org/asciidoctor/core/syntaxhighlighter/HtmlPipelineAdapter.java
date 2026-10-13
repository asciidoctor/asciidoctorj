package org.asciidoctor.core.syntaxhighlighter;

import java.util.Map;

import org.asciidoctor.core.AbstractBlock;

/**
 * A syntax highlighter adapter for html-pipeline, which produces markup compatible with the html-pipeline filters.
 */
public class HtmlPipelineAdapter extends AbstractSyntaxHighlighter {

    public HtmlPipelineAdapter(String name, String backend, Map<String, Object> opts) {
        super(name, backend, opts);
    }

    @Override
    public String format(AbstractBlock node, String lang, Map<String, Object> opts) {
        return "<pre" + (lang != null ? " lang=\"" + lang + "\"" : "") + "><code>" + node.getContent()
                + "</code></pre>";
    }
}
