package org.asciidoctor.core.log;

import org.asciidoctor.core.Logger.Severity;
import org.asciidoctor.core.Reader.Cursor;

/**
 * A message logged by the processor.
 *
 * @param severity the severity of the message
 * @param cursor the location in the source document the message refers to, or null
 * @param message the text of the message
 * @param sourceFileName the name of the file that logged the message, or null
 * @param sourceMethodName the name of the method that logged the message, or null
 */
public record LogRecord(Severity severity, Cursor cursor, String message, String sourceFileName,
        String sourceMethodName) {

    public LogRecord(Severity severity, String message) {
        this(severity, null, message, null, null);
    }

    public LogRecord(Severity severity, Cursor cursor, String message) {
        this(severity, cursor, message, null, null);
    }

    // accessors named like the ones of AsciidoctorJ

    public Severity getSeverity() {
        return severity;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public String getMessage() {
        return message;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }
}
