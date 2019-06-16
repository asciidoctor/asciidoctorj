package org.asciidoctor.syntaxhighlighter;

import org.asciidoctor.ast.Block;

import java.util.Map;

/**
 * A Highlighter transforms the actual raw source text.
 * While a {@link SyntaxHighlighterAdapter} alone might only include the necessary stylesheets and
 * scripts to highlight the sources in the browser, a {@link Highlighter} can actually transform the
 * source to static HTML.
 *
 * <p>If a {@link SyntaxHighlighterAdapter} also implements this interface, the
 * method {@link #highlight(Block, String, String, Map)} will be called for every source block.</p>
 *
 * <p>This API is experimental and might change in an incompatible way in a minor version update!</p>
 */
public interface Highlighter extends SyntaxHighlighterAdapter {

    /**
     * Highlights the source according to the language.
     * @param node The node of the Asciidoctor AST that contains the source to be highlighted.
     * @param source The full content of the source block as one String.
     * @param lang The language that was associated with the source block, e.g. <code>java</code> or
     *             <code>ruby</code>.
     * @param options A map containing options for conversion:
     *                <dl>
     *                     <dt><code>callouts</code></dt>
     *                     <dd>A map containing all callouts indexed by line number (1-based) (optional).
     *                         That means <code>((Map)options.get("callouts")).get(1L)</code> will
     *                         return the List of callouts in the first line.
     *                         The value for each callout is a List with 2 elements:
     *                         The optional comment text before the callout, e.g. <code>// </code>,
     *                         and the callout number, e.g <code>"1"</code>.
     *                     </dd>
     *
     *                     <dt>css_mode</dt>
     *                     <dd>The CSS mode as a String, <code>"class"</code> or
     *                         <code>"inline"</code>
     *                     </dd>
     *
     *                     <dt>highlight_lines</dt>
     *                     <dd>A 1-based Array of Integer line numbers to highlight (aka emphasize) (optional).</dd>
     *
     *                     <dt>number_lines</dt>
     *                     <dd>A String indicating whether lines should be numbered
     *                         (<code>"table"</code> or <code>"inline"</code>) (optional).</dd>
     *
     *                     <dt>start_line_number</dt>
     *                     <dd>The starting Integer (1-based) line number (optional, default: 1).</dd>
     *
     *                     <dt>style</dt>
     *                     <dd>The String style (aka theme) to use for colorizing the code (optional).</dd>
     *
     *                </dl>
     * @return A {@link HighlightResult} containing the converted HTML as a String and optionally a
     * line offset.
     */
    HighlightResult highlight(Block node, String source, String lang, Map<String, Object> options);

}
