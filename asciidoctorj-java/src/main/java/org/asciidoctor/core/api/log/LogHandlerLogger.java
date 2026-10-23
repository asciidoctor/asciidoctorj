package org.asciidoctor.core.api.log;

import java.util.List;

import org.asciidoctor.core.Logger;
import org.asciidoctor.log.LogHandler;

/**
 * A logger of the implementation that forwards all the messages (whatever their severity) to the {@link LogHandler}s
 * of the AsciidoctorJ API (like the JavaLogger of AsciidoctorJ).
 */
public class LogHandlerLogger extends Logger {
    private final List<LogHandler> handlers;

    public LogHandlerLogger(List<LogHandler> handlers) {
        super(null, Severity.DEBUG);
        this.handlers = handlers;
    }

    @Override
    public boolean add(Severity severity, Object message) {
        if (severity == null) {
            severity = Severity.UNKNOWN;
        }
        updateMaxSeverity(severity);
        var logRecord = LogRecords.toLogRecord(severity, message);
        for (LogHandler handler : handlers) {
            handler.log(logRecord);
        }
        return true;
    }
}
