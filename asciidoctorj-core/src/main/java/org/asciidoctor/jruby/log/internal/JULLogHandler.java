package org.asciidoctor.jruby.log.internal;

import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JULLogHandler implements LogHandler {

    private final static String LOGGER_NAME = "asciidoctor";
    private final static Logger LOGGER = Logger.getLogger(LOGGER_NAME);

    @Override
    public void log(LogRecord logRecord) {
        final java.util.logging.LogRecord julLogRecord =
            new java.util.logging.LogRecord(
                mapSeverity(logRecord.getSeverity()),
                logRecord.getCursor() != null ? logRecord.getCursor().toString() + ": " + logRecord.getMessage() : logRecord.getMessage());

        julLogRecord.setSourceClassName(logRecord.getSourceFileName());
        julLogRecord.setSourceMethodName(logRecord.getSourceMethodName());
        julLogRecord.setParameters(new Object[] { logRecord.getCursor() });
        julLogRecord.setLoggerName(LOGGER_NAME);
        LOGGER.log(julLogRecord);
    }

    private static Level mapSeverity(Severity severity) {
        switch (severity) {
            case DEBUG:
                return Level.FINEST;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARNING;
            case ERROR:
                return Level.SEVERE;
            case FATAL:
                return Level.SEVERE;
            case UNKNOWN:
            default:
                return Level.INFO;
        }
    }

}
