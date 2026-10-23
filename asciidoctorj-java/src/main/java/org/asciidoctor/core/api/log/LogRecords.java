package org.asciidoctor.core.api.log;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.core.LogMessage;
import org.asciidoctor.core.Logger;
import org.asciidoctor.core.api.ast.CursorImpl;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

/**
 * Converts the log messages of the implementation to the log records of the AsciidoctorJ API, and vice versa.
 */
public final class LogRecords {

    private LogRecords() {
    }

    public static Severity toSeverity(Logger.Severity severity) {
        return severity == null ? Severity.UNKNOWN : Severity.valueOf(severity.name());
    }

    public static Logger.Severity toSeverity(Severity severity) {
        return severity == null ? Logger.Severity.UNKNOWN : Logger.Severity.valueOf(severity.name());
    }

    /**
     * @return the log record of the API that corresponds to the message logged by the implementation (a String or a
     * {@link LogMessage} with a source location)
     */
    public static LogRecord toLogRecord(Logger.Severity severity, Object message) {
        if (message instanceof LogMessage logMessage) {
            return new LogRecord(toSeverity(severity), CursorImpl.wrap(logMessage.sourceLocation()), logMessage.text());
        }
        return new LogRecord(toSeverity(severity), String.valueOf(message));
    }

    /**
     * @return the message to log with the logger of the implementation that corresponds to the log record of the API
     */
    public static Object toMessage(LogRecord logRecord) {
        Cursor cursor = logRecord.getCursor();
        if (cursor != null) {
            return new LogMessage(logRecord.getMessage(), CursorImpl.unwrap(cursor));
        }
        return logRecord.getMessage();
    }
}
