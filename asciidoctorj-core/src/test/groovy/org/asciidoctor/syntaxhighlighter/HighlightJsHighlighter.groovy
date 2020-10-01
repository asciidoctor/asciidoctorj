package org.asciidoctor.syntaxhighlighter

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.LocationType

class HighlightJsHighlighter implements SyntaxHighlighterAdapter, Formatter {

    static DOCINFO_HEADER = '<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/styles/github.min.css">'
    static DOCINFO_FOOTER = '''<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/highlight.min.js"></script>
<script>hljs.initHighlighting()</script>'''

    boolean hasDocInfo(LocationType location) {
        true
    }

    @Override
    String getDocinfo(LocationType location, Document document, Map<String, Object> options) {
        location == LocationType.HEADER ? DOCINFO_HEADER : DOCINFO_FOOTER
    }

    @Override
    String format(Block node, String lang, Map<String, Object> opts) {
        """<pre class="highlightjs highlight"><code data-lang="$lang" class="language-$lang hljs">${node.content}</code></pre>"""
    }
}

class ObservableHighlightJsHighlighter extends HighlightJsHighlighter {

    static int called = 0

    @Override
    String format(Block node, String lang, Map<String, Object> opts) {
        called++
        super.format(node, lang, opts)
    }
}
