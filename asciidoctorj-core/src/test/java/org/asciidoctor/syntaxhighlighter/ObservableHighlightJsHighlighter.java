package org.asciidoctor.syntaxhighlighter;

import java.util.Map;

import org.asciidoctor.ast.Block;

public class ObservableHighlightJsHighlighter extends HighlightJsHighlighter {

    static int called = 0;

    @Override
    public String format(Block node, String lang, Map<String, Object> opts) {
        called++;
        return super.format(node, lang, opts);
    }
}
