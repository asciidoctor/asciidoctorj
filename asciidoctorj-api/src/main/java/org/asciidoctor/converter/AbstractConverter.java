package org.asciidoctor.converter;

import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Logging;

import java.util.Map;

/**
 * An abstract base class for Java based converters.
 * Instead of directly implementing {@link Converter} Java converters should extend this class.
 * <p>For converters producing string output consider extending the {@link StringConverter} class.
 * @param <T> The type of result this converter produces, e.g. {@link String}.
 */
public abstract class AbstractConverter<T> implements Converter<T>, OutputFormatWriter<T>, Logging, LogHandler {

    private String backend;

    private Map<String, Object> options;

    private String outfilesuffix = null;

    private LogHandler logHandler;

    public AbstractConverter(String backend, Map<String, Object> opts) {
        this.backend = backend;
        this.options = opts;
    }

    @Override
    public Map<String, Object> getOptions() {
        return options;
    }

    public String getBackend() {
        return backend;
    }

    @Override
    public String getOutfileSuffix() {
        return outfilesuffix;
    }

    @Override
    public void setOutfileSuffix(String outfilesuffix) {
        this.outfilesuffix = outfilesuffix;
    }

    @Override
    public void log(LogRecord logRecord) {
        if (logHandler != null) {
            logHandler.log(logRecord);
        }
    }

    @Override
    public void setLogHandler(LogHandler logHandler) {
        this.logHandler = logHandler;
    }
}
