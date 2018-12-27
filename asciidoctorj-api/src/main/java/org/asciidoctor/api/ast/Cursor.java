package org.asciidoctor.api.ast;

public interface Cursor {

    /**
     * @return The line number where the owning block begins.
     */
    int getLineNumber();

    String getPath();

    String getDir();

    String getFile();

}
