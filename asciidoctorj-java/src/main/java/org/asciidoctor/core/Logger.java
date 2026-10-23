package org.asciidoctor.core;

import java.io.PrintStream;
import java.util.function.Supplier;

/**
 * The default logger used by Asciidoctor, which writes formatted messages to a stream (stderr by default) and tracks
 * the maximum severity of messages it has logged.
 */
public class Logger {
    /** The severity levels of log messages. */
    public enum Severity {
        DEBUG, INFO, WARN, ERROR, FATAL, UNKNOWN;

        /**
         * @return the label used when formatting a message of this severity
         */
        public String label() {
            return switch (this) {
                case WARN -> "WARNING";
                case FATAL -> "FAILED";
                default -> name();
            };
        }
    }

    /** Formats a log message. */
    @FunctionalInterface
    public interface Formatter {
        String format(Severity severity, String progname, Object message);
    }

    /** The basic formatter, which produces messages in the form progname: SEVERITY: message. */
    public static final Formatter BASIC_FORMATTER = (severity, progname, message) ->
            progname + ": " + severity.label() + ": " + message + Asciidoctor.LF;

    private PrintStream out;
    private Severity level;
    private Severity maxSeverity;
    private String progname = "asciidoctor";
    private Formatter formatter = BASIC_FORMATTER;

    public Logger() {
        this(System.err);
    }

    public Logger(PrintStream out) {
        this(out, Severity.WARN);
    }

    public Logger(PrintStream out, Severity level) {
        this.out = out;
        this.level = level;
    }

    /**
     * Logs the message with the specified severity, provided the severity meets or exceeds the level of this logger.
     *
     * @param severity the severity of the message
     * @param message the message, either a String or a {@link LogMessage}
     * @return true
     */
    public boolean add(Severity severity, Object message) {
        if (severity == null) {
            severity = Severity.UNKNOWN;
        }
        updateMaxSeverity(severity);
        if (out != null && severity.compareTo(level) >= 0) {
            if (message instanceof Supplier<?> supplier) {
                message = supplier.get();
            }
            out.print(formatter.format(severity, progname, message));
            out.flush();
        }
        return true;
    }

    /**
     * Records the severity as the max severity if it exceeds the current max severity.
     */
    protected void updateMaxSeverity(Severity severity) {
        if (maxSeverity == null || severity.compareTo(maxSeverity) > 0) {
            maxSeverity = severity;
        }
    }

    public void debug(Object message) {
        add(Severity.DEBUG, message);
    }

    public void debug(Supplier<?> message) {
        add(Severity.DEBUG, message);
    }

    public void info(Object message) {
        add(Severity.INFO, message);
    }

    public void info(Supplier<?> message) {
        add(Severity.INFO, message);
    }

    public void warn(Object message) {
        add(Severity.WARN, message);
    }

    public void error(Object message) {
        add(Severity.ERROR, message);
    }

    public void fatal(Object message) {
        add(Severity.FATAL, message);
    }

    public boolean isDebug() {
        return level.compareTo(Severity.DEBUG) <= 0;
    }

    public boolean isInfo() {
        return level.compareTo(Severity.INFO) <= 0;
    }

    public Severity getLevel() {
        return level;
    }

    public void setLevel(Severity level) {
        this.level = level;
    }

    /**
     * @return the maximum severity of the messages logged so far, or null if no messages have been logged
     */
    public Severity getMaxSeverity() {
        return maxSeverity;
    }

    public String getProgname() {
        return progname;
    }

    public void setProgname(String progname) {
        this.progname = progname;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    protected PrintStream getOut() {
        return out;
    }

    protected void setOut(PrintStream out) {
        this.out = out;
    }
}
