package org.asciidoctor.log;

import org.asciidoctor.api.ast.Cursor;

public class LogRecord {

    private final Severity severity;

    private final Cursor cursor;

    private final String message;

    private final String sourceFileName;

    private final String sourceMethodName;

    public LogRecord(Severity severity, Cursor cursor, String message, String sourceFileName, String sourceMethodName) {
        this.severity = severity;
        this.cursor = cursor;
        this.message = message;
        this.sourceFileName = sourceFileName;
        this.sourceMethodName = sourceMethodName;
    }

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
