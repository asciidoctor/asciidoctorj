package org.asciidoctor.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Records the time it takes to read, parse, convert and write a document.
 */
public class Timings {
    // NOTE package-private so the tests can seed the timings
    final Map<String, Double> log = new HashMap<>();
    private final Map<String, Long> timers = new HashMap<>();

    public void start(String key) {
        timers.put(key, System.nanoTime());
    }

    public void record(String key) {
        Long start = timers.remove(key);
        if (start != null) {
            log.put(key, (System.nanoTime() - start) / 1e9);
        }
    }

    /**
     * @return the total time recorded for the specified keys (in seconds), or null if no time was recorded
     */
    public Double time(String... keys) {
        double time = 0;
        for (String key : keys) {
            time += log.getOrDefault(key, 0.0);
        }
        return time > 0 ? time : null;
    }

    public Double getRead() {
        return time("read");
    }

    public Double getParse() {
        return time("parse");
    }

    public Double getReadParse() {
        return time("read", "parse");
    }

    public Double getConvert() {
        return time("convert");
    }

    public Double getReadParseConvert() {
        return time("read", "parse", "convert");
    }

    public Double getWrite() {
        return time("write");
    }

    public Double getTotal() {
        return time("read", "parse", "convert", "write");
    }

    /**
     * Prints the timings report to the specified target (e.g., a PrintStream or a StringBuilder).
     *
     * @param to the target to which the report is printed
     * @param subject the input file being converted, if any
     */
    public void printReport(Appendable to, String subject) {
        try {
            if (subject != null) {
                to.append("Input file: ").append(subject).append('\n');
            }
            to.append("  Time to read and parse source: ").append(format(getReadParse())).append('\n');
            to.append("  Time to convert document: ").append(format(getConvert())).append('\n');
            to.append("  Total time (read, parse and convert): ").append(format(getReadParseConvert())).append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void printReport(Appendable to) {
        printReport(to, null);
    }

    public void printReport() {
        printReport(System.out, null);
    }

    private static String format(Double value) {
        return String.format(Locale.ROOT, "%05.5f", value == null ? 0.0 : value);
    }
}
