package org.asciidoctor.core;

import org.asciidoctor.core.Reader.Cursor;

/**
 * A log message that carries context, such as the location in the source where the message originated.
 *
 * @param text the message text
 * @param sourceLocation the location in the source document (may be null)
 * @param includeLocation the location in the include file (may be null)
 */
public record LogMessage(String text, Cursor sourceLocation, Cursor includeLocation) {

    public LogMessage(String text, Cursor sourceLocation) {
        this(text, sourceLocation, null);
    }

    /**
     * Returns the formatted message, which is the text prefixed with the source location, if present.
     */
    @Override
    public String toString() {
        return sourceLocation == null ? text : sourceLocation + ": " + text;
    }
}
