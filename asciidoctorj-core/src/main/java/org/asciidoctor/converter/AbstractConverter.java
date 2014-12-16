package org.asciidoctor.converter;

import java.util.Map;

public abstract class AbstractConverter implements Converter {

    private String backend;
    private Map<Object, Object> options;

    public AbstractConverter(String backend, Map<Object, Object> opts) {
        this.backend = backend;
        this.options = opts;
    }

    public Map<Object, Object> getOptions() {
        return options;
    }

    public String getBackend() {
        return backend;
    }
}
