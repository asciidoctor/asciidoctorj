package org.asciidoctor.core.syntaxhighlighter;

import java.util.Map;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.Document;

/**
 * A pluggable adapter for integrating a syntax (aka code) highlighter into AsciiDoc processing.
 *
 * <p>There are two types of syntax highlighter adapters. The first performs syntax highlighting during the convert
 * phase. This adapter type must define an {@link #isHighlight()} method that returns true. The companion
 * {@link #highlight} method will then be called to handle the specialcharacters substitution for source blocks. The
 * second assumes syntax highlighting is performed on the client (e.g., when the HTML document is loaded). This adapter
 * type must define a {@link #hasDocinfo(String)} method that returns true. The companion {@link #getDocinfo} method
 * will then be called to insert markup into the output document. The docinfo functionality is available to both
 * adapter types.
 *
 * <p>Asciidoctor provides several built-in adapters, including highlight.js, html-pipeline, and prettify. Additional
 * adapters can be registered using {@link SyntaxHighlighter#register(Object, String...)} or by supplying a custom
 * factory.
 */
public interface SyntaxHighlighter {

    /**
     * The result of highlighting source code.
     *
     * @param text the highlighted source
     * @param sourceOffset the number of lines by which the source was shifted (may be null)
     */
    record HighlightResult(String text, Integer sourceOffset) {
        public HighlightResult(String text) {
            this(text, null);
        }
    }

    /**
     * A function that creates a syntax highlighter (typically a constructor reference).
     */
    @FunctionalInterface
    interface Constructor {
        SyntaxHighlighter create(String name, String backend, Map<String, Object> opts);
    }

    /**
     * @return the String name of this syntax highlighter for referencing it in messages and option names.
     */
    String getName();

    /**
     * Indicates whether this syntax highlighter has docinfo (i.e., markup) to insert into the output document at the
     * specified location. Should be called by converter after main content has been converted.
     *
     * @param location The String representing the location slot (head or footer).
     * @return a Boolean indicating whether the docinfo method should be called for this location.
     */
    default boolean hasDocinfo(String location) {
        return false;
    }

    /**
     * Generates docinfo markup for this syntax highlighter to insert at the specified location in the output document.
     * Should be called by converter after main content has been converted.
     *
     * @param location The String representing the location slot (head or footer).
     * @param doc The Document in which this syntax highlighter is being used.
     * @param opts A Map of options that configure the syntax highlighting:
     *            <ul>
     *            <li>linkcss - A Boolean indicating whether the stylesheet should be linked instead of embedded
     *            (optional).</li>
     *            <li>cdn_base_url - The String base URL for assets loaded from the CDN.</li>
     *            <li>self_closing_tag_slash - The String '/' if the converter calling this method emits self-closing
     *            tags.</li>
     *            </ul>
     * @return the String markup to insert.
     */
    default String getDocinfo(String location, Document doc, Map<String, Object> opts) {
        throw new UnsupportedOperationException(getClass().getName()
                + " must implement the getDocinfo method since hasDocinfo returns true");
    }

    /**
     * Indicates whether highlighting is handled by this syntax highlighter or by the client.
     *
     * @return a Boolean indicating whether the highlight method should be used to handle the specialchars
     *         substitution.
     */
    default boolean isHighlight() {
        return false;
    }

    /**
     * Highlights the specified source when this source block is being converted.
     *
     * <p>If the source contains callout marks, the caller assumes the source remains on the same lines and no closing
     * tags are added to the end of each line. If the source gets shifted by one or more lines, this method must return
     * a result containing the highlighted source and the number of lines by which the source was shifted.
     *
     * @param node The source Block to syntax highlight.
     * @param source The raw source text String of this source block (after preprocessing).
     * @param lang The source language String specified on this block (e.g., ruby).
     * @param opts A Map of options that configure the syntax highlighting:
     *            <ul>
     *            <li>callouts - A Map of callouts extracted from the source, indexed by line number (1-based)
     *            (optional).</li>
     *            <li>css_mode - The String CSS mode (class or inline).</li>
     *            <li>highlight_lines - A 1-based List of Integer line numbers to highlight (aka emphasize)
     *            (optional).</li>
     *            <li>number_lines - A String indicating whether lines should be numbered (table or inline)
     *            (optional).</li>
     *            <li>start_line_number - The starting Integer (1-based) line number (optional, default: 1).</li>
     *            <li>style - The String style (aka theme) to use for colorizing the code (optional).</li>
     *            </ul>
     * @return the highlighted source and an optional line offset.
     */
    default HighlightResult highlight(AbstractBlock node, String source, String lang, Map<String, Object> opts) {
        throw new UnsupportedOperationException(getClass().getName()
                + " must implement the highlight method since isHighlight returns true");
    }

    /**
     * Format the highlighted source for inclusion in an HTML document.
     *
     * @param node The source Block being processed.
     * @param lang The source language String for this Block (e.g., ruby).
     * @param opts A Map of options that control syntax highlighting:
     *            <ul>
     *            <li>nowrap - A Boolean that indicates whether wrapping should be disabled (optional).</li>
     *            </ul>
     * @return the highlighted source String wrapped in preformatted tags (e.g., pre and code)
     */
    String format(AbstractBlock node, String lang, Map<String, Object> opts);

    /**
     * Indicates whether this syntax highlighter wants to write a stylesheet to disk. Only called if both the linkcss and
     * copycss attributes are set on the document.
     *
     * @param doc The Document in which this syntax highlighter is being used.
     * @return a Boolean indicating whether the writeStylesheet method should be called.
     */
    default boolean isWriteStylesheet(Document doc) {
        return false;
    }

    /**
     * Writes the stylesheet to support the highlighted source(s) to disk.
     *
     * @param doc The Document in which this syntax highlighter is being used.
     * @param toDir The absolute String path of the stylesheet output directory.
     */
    default void writeStylesheet(Document doc, String toDir) {
        throw new UnsupportedOperationException(getClass().getName()
                + " must implement the writeStylesheet method since isWriteStylesheet returns true");
    }

    /**
     * @return the global (default) syntax highlighter factory.
     */
    static SyntaxHighlighterFactory getFactory() {
        return SyntaxHighlighterFactory.getDefault();
    }

    /**
     * Statically register the syntax highlighter (a class, a {@link Constructor} or an instance) in the default
     * registry for the specified names.
     */
    static void register(Object syntaxHighlighter, String... names) {
        getFactory().register(syntaxHighlighter, names);
    }

    /**
     * Retrieves the syntax highlighter (class, constructor or instance) registered for the specified name in the
     * default registry.
     */
    static Object forName(String name) {
        return getFactory().forName(name);
    }

    /**
     * Resolves the name to a syntax highlighter instance, if found in the default registry.
     */
    static SyntaxHighlighter create(String name, String backend, Map<String, Object> opts) {
        return getFactory().create(name, backend, opts);
    }
}
