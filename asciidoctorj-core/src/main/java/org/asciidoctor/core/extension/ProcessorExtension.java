package org.asciidoctor.core.extension;

import java.util.Map;

/**
 * A proxy object for an extension implementation such as a processor. It allows the preparation of the extension
 * instance to be separated from its usage to provide consistency between different interfaces and avoid tight coupling
 * with the extension type.
 *
 * <p>The proxy encapsulates the extension kind (e.g., block), its config Map and the extension instance. This proxy is
 * what gets stored in the extension registry when activated.
 *
 * @param <T> the type of the processor
 */
public class ProcessorExtension<T extends Processor> {
    private final String kind;
    private final T instance;

    public ProcessorExtension(String kind, T instance) {
        this.kind = kind;
        this.instance = instance;
    }

    public String getKind() {
        return kind;
    }

    public T getInstance() {
        return instance;
    }

    public Map<String, Object> getConfig() {
        return instance.getConfig();
    }
}
