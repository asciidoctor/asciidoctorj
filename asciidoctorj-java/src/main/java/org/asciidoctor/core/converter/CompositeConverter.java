package org.asciidoctor.core.converter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.asciidoctor.core.AbstractNode;

/**
 * A {@link Converter} implementation that delegates to the chain of {@link Converter} objects passed to the
 * constructor. Selects the first {@link Converter} that identifies itself as the handler for a given transform.
 */
public class CompositeConverter<T> extends AbstractConverter<T> {
    private final List<Converter<T>> converters;
    private final Map<String, Converter<T>> converterCache = new ConcurrentHashMap<>();

    /**
     * Creates a composite converter.
     *
     * @param backend the backend name
     * @param backendTraitsSource the converter from which to copy the backend traits (may be null)
     * @param converters the chain of converters
     */
    @SafeVarargs
    public CompositeConverter(String backend, BackendTraits backendTraitsSource, Converter<T>... converters) {
        super(backend);
        this.converters = List.of(converters);
        if (backendTraitsSource != null) {
            initBackendTraits(backendTraitsSource);
        }
    }

    /**
     * @return the List of Converter objects in the chain
     */
    public List<Converter<T>> getConverters() {
        return converters;
    }

    /**
     * Delegates to the first converter that identifies itself as the handler for the given transform. The optional Map
     * is passed as the last option to the delegate's convert method.
     *
     * @param node the AbstractNode to convert
     * @param transform the optional String transform, or the name of the node if no transform is specified. (default:
     *            null)
     * @param opts an optional Map that is passed to the delegate's convert method. (default: null)
     * @return the result returned from the delegate's convert method
     */
    @Override
    public T convert(AbstractNode node, String transform, Map<String, Object> opts) {
        if (transform == null) {
            transform = node.getNodeName();
        }
        return converterFor(transform).convert(node, transform, opts);
    }

    @Override
    public boolean handles(String transform) {
        return converters.stream().anyMatch(candidate -> candidate.handles(transform));
    }

    /**
     * Retrieve the converter for the specified transform.
     *
     * @return the matching Converter object
     */
    public Converter<T> converterFor(String transform) {
        return converterCache.computeIfAbsent(transform, this::findConverter);
    }

    /**
     * Find the converter for the specified transform. Raise an exception if no converter is found.
     *
     * @return the matching Converter object
     */
    public Converter<T> findConverter(String transform) {
        for (Converter<T> candidate : converters) {
            if (candidate.handles(transform)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not find a converter to handle transform: " + transform);
    }
}
