package org.asciidoctor.log;

/**
 * API to capture messages generated during conversion.
 */
public interface LogHandler {

    /**
     * Process a log record.
     * This method is called during conversion and will abort completely
     * if an exception is thrown from within.
     *
     * @param logRecord
     */
    void log(LogRecord logRecord);

}
