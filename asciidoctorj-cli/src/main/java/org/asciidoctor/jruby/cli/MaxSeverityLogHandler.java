package org.asciidoctor.jruby.cli;

import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

public class MaxSeverityLogHandler implements LogHandler {

    private Severity maxSeverity = Severity.DEBUG;

    @Override
    public void log(LogRecord logRecord) {
        if (this.maxSeverity.compareTo(logRecord.getSeverity()) < 0) {
            this.maxSeverity = logRecord.getSeverity();
        }
    }

    public Severity getMaxSeverity() {
        return this.maxSeverity;
    }
}
