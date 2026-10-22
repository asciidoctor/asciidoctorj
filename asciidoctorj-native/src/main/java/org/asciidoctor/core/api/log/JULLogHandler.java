package org.asciidoctor.core.api.log;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

/**
 * The default {@link LogHandler}, which logs the records with java.util.logging (logger asciidoctor).
 */
public class JULLogHandler implements LogHandler {
    private static final String LOGGER_NAME = "asciidoctor";
    private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

    @Override
    public void log(LogRecord logRecord) {
        java.util.logging.LogRecord julLogRecord = new java.util.logging.LogRecord(
                mapSeverity(logRecord.getSeverity()),
                logRecord.getCursor() != null ? logRecord.getCursor() + ": " + logRecord.getMessage()
                        : logRecord.getMessage());
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
            case WARN:
                return Level.WARNING;
            case ERROR:
            case FATAL:
                return Level.SEVERE;
            case INFO:
            case UNKNOWN:
            default:
                return Level.INFO;
        }
    }
}
