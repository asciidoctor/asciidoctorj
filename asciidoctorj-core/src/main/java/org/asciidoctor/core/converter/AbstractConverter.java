package org.asciidoctor.core.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.AbstractNode;
import org.asciidoctor.core.Logging;

/**
 * An abstract base class for defining converters that can be used to convert {@link AbstractNode} objects in a parsed
 * AsciiDoc document to a backend format such as HTML or DocBook.
 *
 * <p>Concrete converters register a handler for each transform (typically a node name) using one of the handle
 * methods. The {@link #convert(AbstractNode, String, Map)} method dispatches to the handler that matches the
 * transform.
 */
public abstract class AbstractConverter<T> implements Converter<T>, BackendTraits, Logging {

    /**
     * A handler that converts a node using the specified options.
     */
    @FunctionalInterface
    public interface Handler<T> {
        T convert(AbstractNode node, Map<String, Object> opts);
    }

    protected String backend;
    private final Map<String, Handler<T>> handlers = new HashMap<>();
    private String basebackend;
    private String filetype;
    private String htmlsyntax;
    private String outfilesuffix;
    private boolean supportsTemplates;
    private boolean backendTraitsInitialized;

    /**
     * Creates a new instance of this Converter.
     *
     * @param backend The String backend name (aka format) to which this converter converts.
     * @param opts An options Map (optional, default: {})
     */
    protected AbstractConverter(String backend, Map<String, Object> opts) {
        this.backend = backend;
        ConverterFor converterFor = getClass().getAnnotation(ConverterFor.class);
        if (converterFor != null
                && !(converterFor.format().isEmpty() && converterFor.suffix().isEmpty())) {
            // the backend traits declared by the annotation (the traits that are not declared are derived)
            Values derived = BackendTraits.derive(backend, null);
            String filetype = converterFor.format().isEmpty() ? derived.filetype() : converterFor.format();
            String outfilesuffix = converterFor.suffix().isEmpty() ? "." + filetype : converterFor.suffix();
            initBackendTraits(derived.basebackend(), filetype, "html".equals(filetype) ? "html" : null, outfilesuffix,
                    false);
        }
    }

    protected AbstractConverter(String backend) {
        this(backend, Map.of());
    }

    @Override
    public String getBackend() {
        return backend;
    }

    /**
     * Converts an {@link AbstractNode} by delegating to the handler that matches the transform.
     *
     * <p>If the transform is null, the name of the node is used. If no handler is registered for the transform, a
     * warning is logged and null is returned.
     *
     * @see Converter#convert(AbstractNode, String, Map)
     */
    @Override
    public T convert(AbstractNode node, String transform, Map<String, Object> opts) {
        if (transform == null) {
            transform = node.getNodeName();
        }
        Handler<T> handler = handlers.get(transform);
        if (handler == null) {
            getLogger().warn("missing convert handler for " + transform + " node in " + backend + " backend ("
                    + getClass().getName() + ")");
            return null;
        }
        return handler.convert(node, opts);
    }

    @Override
    public boolean handles(String transform) {
        return handlers.containsKey(transform);
    }

    /**
     * Registers the handler for the specified transform.
     */
    protected void handle(String transform, Handler<T> handler) {
        handlers.put(transform, handler);
    }

    /**
     * Registers the handler for the specified transform, casting the node to the specified type.
     */
    protected <N extends AbstractNode> void handle(String transform, Class<N> type, Function<N, T> handler) {
        handlers.put(transform, (node, opts) -> handler.apply(type.cast(node)));
    }

    /**
     * Removes the handler for the specified transform.
     */
    protected void unhandle(String transform) {
        handlers.remove(transform);
    }

    /**
     * Converts the {@link AbstractNode} using only its converted content.
     *
     * @return the converted String content.
     */
    public String contentOnly(AbstractNode node) {
        return node instanceof AbstractBlock block ? block.getContent() : null;
    }

    /**
     * Skips conversion of the {@link AbstractNode}.
     */
    public String skip(AbstractNode node) {
        return null;
    }

    // --- backend traits ---

    /**
     * Initializes the backend traits using the specified values.
     */
    protected void initBackendTraits(String basebackend, String filetype, String htmlsyntax, String outfilesuffix,
            boolean supportsTemplates) {
        this.basebackend = basebackend;
        this.filetype = filetype;
        this.htmlsyntax = htmlsyntax;
        this.outfilesuffix = outfilesuffix;
        this.supportsTemplates = supportsTemplates;
        this.backendTraitsInitialized = true;
    }

    /**
     * Initializes the backend traits from the specified source.
     */
    protected void initBackendTraits(BackendTraits source) {
        initBackendTraits(source.getBasebackend(), source.getFiletype(), source.getHtmlsyntax(),
                source.getOutfilesuffix(), source.isSupportsTemplates());
    }

    private void ensureBackendTraits() {
        if (!backendTraitsInitialized) {
            Values derived = BackendTraits.derive(backend, null);
            initBackendTraits(derived.basebackend(), derived.filetype(), derived.htmlsyntax(),
                    derived.outfilesuffix(), false);
        }
    }

    @Override
    public String getBasebackend() {
        ensureBackendTraits();
        return basebackend;
    }

    /**
     * Sets the basebackend, deriving the other backend traits from it if they're not yet initialized.
     */
    public void setBasebackend(String value) {
        if (!backendTraitsInitialized) {
            Values derived = BackendTraits.derive(backend, value);
            initBackendTraits(derived.basebackend(), derived.filetype(), derived.htmlsyntax(),
                    derived.outfilesuffix(), false);
        }
        basebackend = value;
    }

    @Override
    public String getFiletype() {
        ensureBackendTraits();
        return filetype;
    }

    public void setFiletype(String value) {
        ensureBackendTraits();
        filetype = value;
    }

    @Override
    public String getHtmlsyntax() {
        ensureBackendTraits();
        return htmlsyntax;
    }

    public void setHtmlsyntax(String value) {
        ensureBackendTraits();
        htmlsyntax = value;
    }

    @Override
    public String getOutfilesuffix() {
        ensureBackendTraits();
        return outfilesuffix;
    }

    public void setOutfilesuffix(String value) {
        ensureBackendTraits();
        outfilesuffix = value;
    }

    @Override
    public boolean isSupportsTemplates() {
        ensureBackendTraits();
        return supportsTemplates;
    }

    public void setSupportsTemplates(boolean value) {
        ensureBackendTraits();
        supportsTemplates = value;
    }
}
