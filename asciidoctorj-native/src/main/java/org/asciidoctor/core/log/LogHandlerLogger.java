package org.asciidoctor.core.log;

import java.util.List;

import org.asciidoctor.core.LogMessage;
import org.asciidoctor.core.Logger;

/**
 * A logger that forwards the messages to {@link LogHandler}s.
 */
public class LogHandlerLogger extends Logger {
    private final List<LogHandler> handlers;

    public LogHandlerLogger(List<LogHandler> handlers) {
        super(null, Severity.DEBUG);
        this.handlers = List.copyOf(handlers);
    }

    @Override
    public boolean add(Severity severity, Object message) {
        if (severity == null) {
            severity = Severity.UNKNOWN;
        }
        updateMaxSeverity(severity);
        LogRecord logRecord = message instanceof LogMessage logMessage
                ? new LogRecord(severity, logMessage.sourceLocation(), logMessage.text())
                : new LogRecord(severity, String.valueOf(message));
        handlers.forEach(handler -> handler.log(logRecord));
        return true;
    }
}
