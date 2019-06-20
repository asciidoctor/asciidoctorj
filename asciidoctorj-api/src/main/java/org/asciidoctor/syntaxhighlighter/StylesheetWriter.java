package org.asciidoctor.syntaxhighlighter;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.LocationType;

import java.io.File;
import java.util.Map;

/**
 * If a {@link SyntaxHighlighterAdapter} also implements this interface it will be called while rendering
 * to write a stylesheet to disc.
 *
 * <p>This only happens if the output is written to file and the attributes <code>:linkcss</code>
 * and <code>:copycss</code> are set.</p>
 *
 * <p>This API is experimental and might change in an incompatible way in a minor version update!</p>
 */
public interface StylesheetWriter {

    /**
     * Has to return <code>true</code> if Asciidoctor should call {@link #writeStylesheet(Document, File)}.
     * @param doc The current document that is rendered.
     * @return <code>true</code> if {@link #writeStylesheet(Document, File)} should be called.
     */
    boolean isWriteStylesheet(Document doc);

    /**
     * Allows to write additional stylesheets to disk while rendering a document.
     * References to this stylesheet should be added by {@link SyntaxHighlighterAdapter#getDocinfo(LocationType, Document, Map)}.
     * @param doc The current document to be rendered.
     * @param toDir The file representing the absolute path to the directory where the stylesheet should be written.
     */
    void writeStylesheet(Document doc, File toDir);

}
