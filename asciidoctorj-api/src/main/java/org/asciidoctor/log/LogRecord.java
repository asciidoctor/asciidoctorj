package org.asciidoctor.log;

import org.asciidoctor.ast.Cursor;

/**
 * Description of an specific event occurred during conversion.
 */
public class LogRecord {

    private final Severity severity;

    private Cursor cursor;

    private final String message;

    private String sourceFileName;

    private String sourceMethodName;

    public LogRecord(Severity severity, String message) {
        this.severity = severity;
        this.message = message;
    }

    public LogRecord(Severity severity, Cursor cursor, String message) {
        this.severity = severity;
        this.cursor = cursor;
        this.message = message;
    }

    public LogRecord(Severity severity, Cursor cursor, String message, String sourceFileName, String sourceMethodName) {
        this.severity = severity;
        this.cursor = cursor;
        this.message = message;
        this.sourceFileName = sourceFileName;
        this.sourceMethodName = sourceMethodName;
    }

    /**
     * @return Severity level of the current record.
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * @return Information about the location of the event.
     */
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * @return Descriptive message about the event.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return The value <code>&lt;script&gt;</code>. For the source filename use {@link Cursor#getFile()} obtained with the {@link #getCursor()} method.
     */
    public String getSourceFileName() {
        return sourceFileName;
    }

    /**
     * @return The Asciidoctor Ruby engine method used to convert the file; <code>convertFile</code> or <code>convert</code> whether you are converting a File or a String.
     */
    public String getSourceMethodName() {
        return sourceMethodName;
    }
}
