package org.asciidoctor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MemoryLogHandler extends Handler {

  private List<LogRecord> logRecords = new ArrayList<>();

  @Override
  public void publish(LogRecord record) {
    logRecords.add(record);
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() throws SecurityException {
    logRecords.clear();
  }

  public List<LogRecord> getLogRecords() {
    return logRecords;
  }
}
