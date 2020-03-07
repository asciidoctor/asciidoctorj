package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import org.asciidoctor.api.extension.LocationType

class HighlightJsHighlighter implements SyntaxHighlighterAdapter, Formatter {

    @Override
    boolean hasDocInfo(LocationType location) {
        location == LocationType.FOOTER
    }

    @Override
    String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
        String baseUrl = document.getAttribute('highlightjsdir', "${options['cdn_base_url']}/highlight.js/9.15.6")
        """<link rel="stylesheet" href="$baseUrl/styles/${document.getAttribute('highlightjs-theme', 'github')}.min.css"${options['self_closing_tag_slash']}>
<script src="$baseUrl/highlight.min.js"></script>
<script>hljs.initHighlighting()</script>"""
    }

    @Override
    String format(Block node, String lang, Map<String, Object> opts) {
        """<pre class="highlightjs highlight"><code data-lang="$lang" class="language-$lang hljs">${node.content}</code></pre>"""
    }
}
