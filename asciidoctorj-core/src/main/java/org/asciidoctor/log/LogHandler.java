package org.asciidoctor.log;

import org.asciidoctor.ast.Cursor;

public interface LogHandler {

    void log(LogRecord logRecord);
}
