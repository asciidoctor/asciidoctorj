package org.asciidoctor.log;

import java.util.ArrayList;
import java.util.List;

public class TestLogHandlerService implements LogHandler {

    private static List<LogRecord> logRecords = new ArrayList<>();

    public static List<LogRecord> getLogRecords() {
        return logRecords;
    }

    public static void clear() {
        logRecords.clear();
    }

    @Override
    public void log(LogRecord logRecord) {
        logRecords.add(logRecord);
    }
}