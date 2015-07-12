package org.asciidoctor.converter;

import java.util.Map;

public abstract class AbstractConverter<T> implements Converter<T> {

    private String backend;

    private Map<String, Object> options;

    private String outfilesuffix = null;

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

}
