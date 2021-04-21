package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;

import java.util.Map;

public interface PreprocessorReader extends Reader {

    /**
     * Push source content onto the front of the reader and switch the context
     * based on the file, document-relative path and line information given.
     * This method is typically used in an {@link IncludeProcessor} to add content 
     * read from the target specified.
     *
     * @deprecated Use {@link #pushInclude(String, String, String, int, Map)} instead.
     *
     * @param data       content to push
     * @param file       representation of name of the included file. Does not need to exists
     * @param path       representation of path of the included file. Does not need to exists
     * @param lineNumber line number of the first line of the included content
     * @param attributes additional attributes to pass
     */
    @Deprecated
    void push_include(String data, String file, String path, int lineNumber, Map<String, Object> attributes);

    /**
     * Push source content onto the front of the reader and switch the context
     * based on the file, document-relative path and line information given.
     * This method is typically used in an {@link IncludeProcessor} to add content 
     * read from the target specified.
     *
     * @param data       content to push
     * @param file       representation of name of the included file. Does not need to exists
     * @param path       representation of path of the included file. Does not need to exists
     * @param lineNumber line number of the first line of the included content
     * @param attributes additional attributes to pass
     */
    void pushInclude(String data, String file, String path, int lineNumber, Map<String, Object> attributes);

    /**
     * @deprecated Please use {@link #getDocument()}
     *
     * @return Document representation.
     */
    @Deprecated
    Document document();

    /**
     * @return Document representation.
     */
    Document getDocument();
}
