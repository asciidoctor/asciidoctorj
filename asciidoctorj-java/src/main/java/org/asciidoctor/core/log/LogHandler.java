package org.asciidoctor.core.log;

/**
 * Receives the messages logged by the processor.
 *
 * <p>To register handlers, set the logger of the processor to a {@link LogHandlerLogger}, e.g.,
 * {@code LoggerManager.setLogger(new LogHandlerLogger(List.of(handler)))}.
 */
@FunctionalInterface
public interface LogHandler {

    void log(LogRecord logRecord);
}
