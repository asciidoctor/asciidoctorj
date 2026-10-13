package org.asciidoctor.core;

import org.asciidoctor.core.Reader.Cursor;

/**
 * Provides access to the logger and helpers to create messages with context.
 */
public interface Logging {

    /**
     * @return the logger managed by {@link LoggerManager}
     */
    default Logger getLogger() {
        return LoggerManager.getLogger();
    }

    /**
     * @return the logger managed by {@link LoggerManager}
     */
    static Logger logger() {
        return LoggerManager.getLogger();
    }

    /**
     * Creates a log message with the specified source location.
     */
    static LogMessage messageWithContext(String text, Cursor sourceLocation) {
        return new LogMessage(text, sourceLocation);
    }

    /**
     * Creates a log message with the specified source and include locations.
     */
    static LogMessage messageWithContext(String text, Cursor sourceLocation, Cursor includeLocation) {
        return new LogMessage(text, sourceLocation, includeLocation);
    }
}
