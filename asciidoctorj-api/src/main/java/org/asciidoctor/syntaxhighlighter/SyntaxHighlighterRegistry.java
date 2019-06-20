package org.asciidoctor.syntaxhighlighter;

/**
 * <p>This API is experimental and might change in an incompatible way in a minor version update!</p>
 */
public interface SyntaxHighlighterRegistry {

    void register(Class<? extends SyntaxHighlighterAdapter> sourceHighlighter , String... names);

}
