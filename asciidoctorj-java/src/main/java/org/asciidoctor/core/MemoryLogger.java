package org.asciidoctor.core;

import java.util.ArrayList;
import java.util.List;

/**
 * A logger that keeps the log messages in memory. Primarily intended for testing and tooling.
 */
public class MemoryLogger extends Logger {
    /**
     * A message recorded by the memory logger.
     *
     * @param severity the severity of the message
     * @param message the message, either a String or a {@link LogMessage}
     */
    public record Message(Severity severity, Object message) {
        /**
         * @return the text of the message
         */
        public String text() {
            return message instanceof LogMessage logMessage ? logMessage.text() : String.valueOf(message);
        }
    }

    private final List<Message> messages = new ArrayList<>();

    public MemoryLogger() {
        super(null, Severity.UNKNOWN);
    }

    @Override
    public boolean add(Severity severity, Object message) {
        messages.add(new Message(severity == null ? Severity.UNKNOWN : severity,
                message instanceof java.util.function.Supplier<?> supplier ? supplier.get() : message));
        return true;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void clear() {
        messages.clear();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    @Override
    public Severity getMaxSeverity() {
        return messages.stream().map(Message::severity).max(Enum::compareTo).orElse(null);
    }
}
