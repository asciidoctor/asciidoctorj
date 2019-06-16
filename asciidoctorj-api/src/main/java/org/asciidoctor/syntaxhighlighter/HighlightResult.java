package org.asciidoctor.syntaxhighlighter;

/**
 * <p>This API is experimental and might change in an incompatible way in a minor version update!</p>
 */
public class HighlightResult {

    private String highlightedSource;

    private Integer lineOffset;

    public HighlightResult(String highlightedSource) {
        this.highlightedSource = highlightedSource;
    }

    public HighlightResult(String highlightedSource, Integer lineOffset) {
        this(highlightedSource);
        this.lineOffset = lineOffset;
    }

    public String getHighlightedSource() {
        return highlightedSource;
    }

    public Integer getLineOffset() {
        return lineOffset;
    }

}
