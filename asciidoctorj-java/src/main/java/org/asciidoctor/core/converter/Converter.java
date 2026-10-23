package org.asciidoctor.core.converter;

import java.util.Map;

import org.asciidoctor.core.AbstractNode;

/**
 * An interface for defining converters that are used to convert {@link AbstractNode} objects in a parsed AsciiDoc
 * document to an output (aka backend) format such as HTML or DocBook.
 *
 * <p>The type parameter is the type of the output: a String for a textual format (e.g., HTML), but it can be any type
 * for a binary format (e.g., the bytes of a PDF document). A converter that produces an output other than a String
 * should implement {@link org.asciidoctor.core.Writer} to write it.
 *
 * <p>A Converter is typically instantiated each time an AsciiDoc document is processed (i.e., parsed and converted).
 * Implementing a custom converter entails:
 *
 * <ul>
 * <li>Implementing the {@link Converter} interface and its {@link #convert(AbstractNode, String, Map)} method or
 * extending the {@link AbstractConverter} class and registering handlers that map to each node.</li>
 * <li>Optionally registering the converter with one or more backend names statically using
 * {@link #register(Object, String...)}.</li>
 * </ul>
 *
 * <pre>{@code
 * public class TextConverter extends AbstractConverter<String> {
 *     public TextConverter(String backend, Map<String, Object> opts) {
 *         super(backend, opts);
 *         setOutfilesuffix(".txt");
 *         handle("document", Document.class, node -> node.getTitle() + "\n\n" + node.getContent());
 *         handle("paragraph", Block.class, node -> node.getContent().replace('\n', ' ') + "\n");
 *     }
 * }
 *
 * Converter.register(TextConverter.class, "text");
 * }</pre>
 */
public interface Converter<T> {

    /**
     * Converts an {@link AbstractNode} using the given transform.
     *
     * @param node The concrete instance of AbstractNode to convert.
     * @param transform An optional String transform that hints at which transformation should be applied to this node.
     *            If a transform is not given, the transform is often derived from the value of the
     *            {@link AbstractNode#getNodeName()} property. (optional, default: null)
     * @param opts An optional Map of options hints about how to convert the node. (optional, default: null)
     * @return the result of the conversion.
     */
    T convert(AbstractNode node, String transform, Map<String, Object> opts);

    default T convert(AbstractNode node, String transform) {
        return convert(node, transform, null);
    }

    default T convert(AbstractNode node) {
        return convert(node, null, null);
    }

    /**
     * Converts the result of a conversion to a String (e.g., to include the converted output of a node in the output
     * of its parent).
     *
     * @return the output if it's a String (or null), otherwise its String representation
     */
    static String asString(Object output) {
        return output == null || output instanceof String ? (String) output : output.toString();
    }

    /**
     * Reports whether the current converter is able to convert this node (by its transform name). Used by the
     * {@link CompositeConverter} to select which converter to use to handle a given node. Returns true by default.
     *
     * @param transform the String name of the node transformation (typically the node name).
     * @return a Boolean indicating whether this converter can handle the specified transform.
     */
    default boolean handles(String transform) {
        return true;
    }

    /**
     * @return the String backend name that this converter is handling.
     */
    default String getBackend() {
        return null;
    }

    /**
     * A function that creates a converter for the specified backend and options (typically a constructor reference).
     */
    @FunctionalInterface
    interface Constructor {
        Converter<?> create(String backend, Map<String, Object> opts);
    }

    /**
     * @return the global (default) converter factory, which includes the built-in converters.
     */
    static ConverterFactory getFactory() {
        return ConverterFactory.getDefault();
    }

    /**
     * Registers the converter (a Converter class, a {@link Constructor} or a Converter instance) with the default
     * registry to handle the specified backend name(s).
     *
     * @param converter the converter to register
     * @param backends one or more String backend names with which to associate this converter
     */
    static void register(Object converter, String... backends) {
        getFactory().register(converter, backends.length > 0 ? backends : new String[] { backendFor(converter) });
    }

    /**
     * Resolves the backend declared by the {@link ConverterFor} annotation of the converter (class or instance).
     *
     * @throws IllegalArgumentException if the converter doesn't declare a backend
     */
    static String backendFor(Object converter) {
        Class<?> type = converter instanceof Class<?> clazz ? clazz : converter.getClass();
        ConverterFor converterFor = type.getAnnotation(ConverterFor.class);
        if (converterFor != null) {
            String backend = converterFor.value().isEmpty() ? converterFor.backend() : converterFor.value();
            if (!backend.isEmpty()) {
                return backend;
            }
        }
        throw new IllegalArgumentException("No backend specified for converter " + type.getName()
                + " (specify the backend or annotate the class with @ConverterFor)");
    }

    /**
     * Lookup the converter registered with the default registry to handle the specified backend.
     *
     * @param backend the String backend name.
     * @return the converter (class, constructor or instance) registered to convert the specified backend or null if no
     *         match is found.
     */
    static Object forBackend(String backend) {
        return getFactory().forBackend(backend);
    }

    /**
     * Create a new Converter object that can be used to convert {@link AbstractNode}s to the format associated with
     * the backend using the default registry.
     *
     * @param backend the String backend name.
     * @param opts a Map of options to customize creation; also passed to the converter's constructor
     * @return the Converter instance.
     */
    static Converter<?> create(String backend, Map<String, Object> opts) {
        return getFactory().create(backend, opts);
    }

    static Converter<?> create(String backend) {
        return create(backend, Map.of());
    }
}
