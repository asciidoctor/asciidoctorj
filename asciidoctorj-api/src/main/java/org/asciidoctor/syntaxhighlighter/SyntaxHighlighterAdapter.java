package org.asciidoctor.syntaxhighlighter;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;

import java.util.Map;

/**
 * This interface has to be implemented by all syntax highlighter adapters, regardless if they
 * update the resulting HTML document to highlight sources on the client or on the server.
 *
 * <p>Depending on how the syntax highlighter works in detail one or more of these interfaces
 * should be implemented as well:
 * <dl>
 *     <dt>{@link Formatter}</dt>
 *     <dd>If the highlighter requires certain classes on the
 *     <code>&lt;pre&gt;&lt;code&gt;&lt;/code&gt;&lt;/pre&gt;</code>
 *     elements that enclose the source text.</dd>
 *
 *     <dt>{@link StylesheetWriter}</dt>
 *     <dd>If the highlighter can write stylesheets to external files in case the document
 *     is rendered with the attributes <code>:linkcss</code> and <code>:copycss</code>.</dd>
 *
 *     <dt>{@link Highlighter}</dt>
 *     <dd>If the highlighter actually highlights the source text while rendering the asciidoc
 *     document to html.</dd>
 *
 *     <dt>{@link org.asciidoctor.log.Logging}</dt>
 *     <dd>If the highlighter wants to log to the common Asciidoctor logger.</dd>
 * </dl>
 *
 * <p>SyntaxHighlighterAdapters have to be registered as classes at the
 * {@link SyntaxHighlighterRegistry}:
 * <pre><code>
 *     Asciidoctor asciidoctor;
 *     asciidoctor.syntaxHighlighterRegistry().register(MySyntaxHighligher.class, "mysyntaxhighighter");
 *     asciidoctor.convert(doc,
 *         OptionsBuilder.options()
 *             .attributes(AttributesBuilder.attributes()
 *                 .sourceHighlighter("mysyntaxhighlighter")));</code></pre>
 * </p>
 * <p>A SyntaxHighlighterAdapter is expected to have a constructor with 3 parameters:
 * <dl>
 *     <dt>name</dt>
 *     <dd>A <code>String</code> containing the name of the highlighter.</dd>
 *
 *     <dt>backend</dt>
 *     <dd>A <code>String</code> containing the backend used for rendering, e.g. <code>"html5"</code>.</dd>
 *
 *     <dt>options</dt>
 *     <dd>A <code>Map&lt;String, Object&gt;</code> that contains options for rendering.
 *     One key that is always present is <code>document</code> that contains the current
 *     {@link Document} that is rendered.</dd>
 * </dl>
 * All parameters are optional, that means if there is only a constructor that takes two
 * <code>String</code> parameters, then this will be used to construct an instance.
 * This includes using the default constructor if the class defines no constructor at all.
 * </p>
 * <p>The lifecycle of a SyntaxHighlighter is that one instance is created for every document that
 * is converted.</p>
 *
 * <p>This API is experimental and might change in an incompatible way in a minor version update!</p>
 */
public interface SyntaxHighlighterAdapter {

  boolean hasDocInfo(LocationType location);

  String getDocinfo(LocationType location, Document document, Map<String, Object> options);

}
