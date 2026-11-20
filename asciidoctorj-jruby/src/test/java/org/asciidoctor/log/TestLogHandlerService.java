package org.asciidoctor.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestLogHandlerService implements LogHandler {

    private static List<LogRecord> logRecords = new ArrayList<>();

    public static List<LogRecord> getLogRecords() {
        return logRecords;
    }

    public static void clear() {
        logRecords.clear();
    }

    public static AtomicInteger instancesCount = new AtomicInteger();

    public TestLogHandlerService() {
        instancesCount.incrementAndGet();
    }

    @Override
    public void log(LogRecord logRecord) {
        logRecords.add(logRecord);
    }
}