package org.asciidoctor.core;

/**
 * A logger that discards all messages, but still tracks the maximum severity.
 */
public class NullLogger extends Logger {
    private Severity maxSeverity;

    public NullLogger() {
        super(null, Severity.UNKNOWN);
    }

    @Override
    public boolean add(Severity severity, Object message) {
        if (severity == null) {
            severity = Severity.UNKNOWN;
        }
        if (maxSeverity == null || severity.compareTo(maxSeverity) > 0) {
            maxSeverity = severity;
        }
        return true;
    }

    @Override
    public Severity getMaxSeverity() {
        return maxSeverity;
    }
}
