package org.asciidoctor.syntaxhighlighter;

import org.asciidoctor.ast.Block;

import java.util.Map;

/**
 * If a {@link SyntaxHighlighterAdapter} also implements the {@link Formatter} interface, then it
 * will be invoked to convert the full source block container including the <code>&lt;pre/&gt;</code>
 * and <code>&lt;code/&gt;</code> elements.
 * This way it is able to assign custom classes to these elements.
 * <p>A client side renderer will usually implement this interface so that it can certain marker
 * classes including an indicator for the language of the source.</p>
 *
 * <p>This API is experimental and might change in an incompatible way in a minor version update!</p>
 */
public interface Formatter {

    /**
     * Format the highlighted source for inclusion in an HTML document.
     * Should call {@link Block#getContent()} to get the content of the source block, which might be
     * highlighted by a {@link Highlighter}.
     *
     * <p>Example:
     * <pre><code>
     *     public String format(Block node, String lang, Map&lt;String, Object&gt; options) {
     *         StringBuilder sb = new StringBuilder();
     *         sb.append("&lt;pre class='highlight'&gt;");
     *         sb.append("&lt;code data-lang='").append(lang).append("'&gt;");
     *         sb.append(node.getContent());
     *         sb.append("&lt;/code&gt;");
     *         sb.append("&lt;/pre&gt;\n");
     *         return sb.toString();
     *     }
     * </code></pre>
     * </p>
     *
     * @param node The source Block being processed.
     * @param lang The source language String for this Block (e.g., ruby).
     * @param opts A Hash of options that control syntax highlighting:
     *             <ul>
     *             <li><code>nowrap</code>: A Boolean that indicates whether wrapping should be disabled (optional).</li>
     *             </ul>
     * @return Returns the highlighted source wrapped in preformatted tags (e.g., pre and code)
     */
    String format(Block node, String lang, Map<String, Object> opts);

}
