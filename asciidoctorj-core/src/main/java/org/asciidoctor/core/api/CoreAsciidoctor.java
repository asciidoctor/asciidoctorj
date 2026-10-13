package org.asciidoctor.core.api;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.core.api.ast.NodeConverter;
import org.asciidoctor.core.api.converter.ConverterProxy;
import org.asciidoctor.core.api.converter.JavaConverterRegistryImpl;
import org.asciidoctor.core.api.extension.ExtensionGroupImpl;
import org.asciidoctor.core.api.extension.JavaExtensionRegistryImpl;
import org.asciidoctor.core.api.extension.RubyExtensionRegistryImpl;
import org.asciidoctor.core.api.log.JULLogHandler;
import org.asciidoctor.core.api.log.LogHandlerLogger;
import org.asciidoctor.core.api.syntaxhighlighter.SyntaxHighlighterProxy;
import org.asciidoctor.core.api.syntaxhighlighter.SyntaxHighlighterRegistryImpl;
import org.asciidoctor.core.converter.Converter;
import org.asciidoctor.core.converter.ConverterFactory;
import org.asciidoctor.core.extension.Group;
import org.asciidoctor.core.extension.Registry;
import org.asciidoctor.core.syntaxhighlighter.SyntaxHighlighter;
import org.asciidoctor.core.syntaxhighlighter.SyntaxHighlighterFactory;
import org.asciidoctor.extension.ExtensionGroup;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.RubyExtensionRegistry;
import org.asciidoctor.jruby.converter.spi.ConverterRegistry;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;
import org.asciidoctor.jruby.internal.AsciidoctorCoreException;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterRegistry;

/**
 * The implementation of the {@link Asciidoctor} interface of the AsciidoctorJ API (registered as a service, so it's
 * created by {@link Asciidoctor.Factory#create()}).
 *
 * <p>Like the instances of AsciidoctorJ (which have their own Ruby runtime), each instance has its own extensions,
 * converters, syntax highlighters and log handlers, which are passed to the processor with the options of each
 * conversion (so they don't affect the other instances).
 */
public class CoreAsciidoctor implements Asciidoctor, LogHandler {
    private final List<LogHandler> logHandlers = new CopyOnWriteArrayList<>();
    private final Map<String, Group> extensionGroups = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<String, Class<?>> converters = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<String, Class<? extends SyntaxHighlighterAdapter>> syntaxHighlighters =
            Collections.synchronizedMap(new LinkedHashMap<>());

    public CoreAsciidoctor() {
        logHandlers.add(new JULLogHandler());
        processRegistrations();
    }

    /**
     * Registers the extensions, converters, syntax highlighters and log handlers that are registered as services.
     */
    private void processRegistrations() {
        for (ExtensionRegistry registry : load(ExtensionRegistry.class)) {
            registry.register(this);
        }
        for (ConverterRegistry registry : load(ConverterRegistry.class)) {
            registry.register(this);
        }
        for (org.asciidoctor.jruby.syntaxhighlighter.spi.SyntaxHighlighterRegistry registry
                : load(org.asciidoctor.jruby.syntaxhighlighter.spi.SyntaxHighlighterRegistry.class)) {
            registry.register(this);
        }
        for (LogHandler logHandler : load(LogHandler.class)) {
            registerLogHandler(logHandler);
        }
    }

    private static <S> Iterable<S> load(Class<S> service) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return ServiceLoader.load(service, classLoader != null ? classLoader : CoreAsciidoctor.class.getClassLoader());
    }

    // --- registries ---

    /**
     * Registers a group of extensions with this instance.
     *
     * @param name the name of the group (a random name is generated if null)
     * @param group the group, which registers the extensions with the registry of the implementation
     */
    public void registerExtensionGroup(String name, Group group) {
        extensionGroups.put(name == null ? UUID.randomUUID().toString() : name, group);
    }

    public void unregisterExtensionGroup(String name) {
        extensionGroups.remove(name);
    }

    /**
     * @return the converters registered with this instance (by backend)
     */
    public Map<String, Class<?>> getConverters() {
        return converters;
    }

    /**
     * @return the syntax highlighters registered with this instance (by name)
     */
    public Map<String, Class<? extends SyntaxHighlighterAdapter>> getSyntaxHighlighters() {
        return syntaxHighlighters;
    }

    @Override
    public JavaExtensionRegistry javaExtensionRegistry() {
        return new JavaExtensionRegistryImpl(this);
    }

    @Override
    public RubyExtensionRegistry rubyExtensionRegistry() {
        return new RubyExtensionRegistryImpl();
    }

    @Override
    public JavaConverterRegistry javaConverterRegistry() {
        return new JavaConverterRegistryImpl(this);
    }

    @Override
    public SyntaxHighlighterRegistry syntaxHighlighterRegistry() {
        return new SyntaxHighlighterRegistryImpl(this);
    }

    @Override
    public ExtensionGroup createGroup() {
        return createGroup(UUID.randomUUID().toString());
    }

    @Override
    public ExtensionGroup createGroup(String groupName) {
        return new ExtensionGroupImpl(groupName, this);
    }

    @Override
    public void unregisterAllExtensions() {
        extensionGroups.clear();
    }

    /**
     * Ruby libraries can't be required since there's no Ruby runtime.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void requireLibrary(String... requiredLibraries) {
        requireLibraries(List.of(requiredLibraries));
    }

    /**
     * Ruby libraries can't be required since there's no Ruby runtime.
     *
     * @throws UnsupportedOperationException if a library is specified
     */
    @Override
    public void requireLibraries(Collection<String> requiredLibraries) {
        if (requiredLibraries != null && !requiredLibraries.isEmpty()) {
            throw new UnsupportedOperationException("Ruby libraries can't be required by this implementation of "
                    + "AsciidoctorJ (there's no Ruby runtime): " + requiredLibraries);
        }
    }

    // --- logging ---

    @Override
    public void registerLogHandler(LogHandler logHandler) {
        if (!logHandlers.contains(logHandler)) {
            logHandlers.add(logHandler);
        }
    }

    @Override
    public void unregisterLogHandler(LogHandler logHandler) {
        logHandlers.remove(logHandler);
    }

    @Override
    public void log(LogRecord logRecord) {
        for (LogHandler logHandler : logHandlers) {
            logHandler.log(logRecord);
        }
    }

    // --- conversion ---

    /**
     * @return the options of the implementation that correspond to the options of the API, with the extensions,
     * converters, syntax highlighters and log handlers of this instance
     */
    @SuppressWarnings("deprecation")
    Map<String, Object> prepareOptions(Options options) {
        Map<String, Object> opts = new LinkedHashMap<>(options == null ? Map.of() : options.map());
        if (opts.get("attributes") instanceof Map<?, ?> attributes) {
            opts.put("attributes", new LinkedHashMap<>(attributes));
        }
        opts.putIfAbsent("logger", new LogHandlerLogger(logHandlers));
        synchronized (extensionGroups) {
            if (!extensionGroups.isEmpty() && !opts.containsKey("extension_registry")) {
                opts.put("extension_registry", new Registry(new LinkedHashMap<>(extensionGroups)));
            }
        }
        synchronized (converters) {
            if (!converters.isEmpty() && !opts.containsKey("converter_factory")) {
                Map<String, Object> constructors = new LinkedHashMap<>();
                converters.forEach((backend, converterClass) -> constructors.put(backend,
                        (Converter.Constructor) (actualBackend, converterOpts) -> new ConverterProxy(converterClass,
                                actualBackend, converterOpts, this)));
                opts.put("converter_factory", ConverterFactory.proxy(constructors));
            }
        }
        synchronized (syntaxHighlighters) {
            if (!syntaxHighlighters.isEmpty() && !opts.containsKey("syntax_highlighter_factory")) {
                Map<String, Object> constructors = new LinkedHashMap<>();
                syntaxHighlighters.forEach((name, highlighterClass) -> constructors.put(name,
                        (SyntaxHighlighter.Constructor) (actualName, backend, hlOpts) -> new SyntaxHighlighterProxy(
                                highlighterClass, actualName, backend, hlOpts, this)));
                opts.put("syntax_highlighter_factory", SyntaxHighlighterFactory.proxy(constructors));
            }
        }
        return opts;
    }

    /**
     * @return the exception thrown when a conversion fails: when the processor reports that the document failed to
     * load (like Asciidoctor does), the exception has its message and the cause of the failure
     */
    private static AsciidoctorCoreException coreException(RuntimeException e) {
        if (e.getCause() != null && e.getMessage() != null && e.getMessage().startsWith("asciidoctor: FAILED: ")) {
            return new AsciidoctorCoreException(e.getMessage(), e.getCause());
        }
        return new AsciidoctorCoreException(e);
    }

    /**
     * @return the result of the conversion as the expected type: a {@link Document} (if the output was written to a
     * file), the converted String or the output of the converter
     */
    private static <T> T adaptReturn(Object result, Class<T> expectedResult) {
        if (result instanceof org.asciidoctor.core.Document document) {
            return Document.class.isAssignableFrom(expectedResult) ? expectedResult.cast(NodeConverter.wrap(document))
                    : null;
        } else if (result == null || expectedResult.isInstance(result)) {
            return expectedResult.cast(result);
        } else if (result instanceof String) {
            // NOTE like AsciidoctorJ, the converted String is not returned if another type is expected
            return null;
        }
        throw new ClassCastException("The result of the conversion is a " + result.getClass().getName() + ", not a "
                + expectedResult.getName());
    }

    @Override
    public String convert(String content, Options options) {
        return convert(content, options, String.class);
    }

    @Override
    public <T> T convert(String content, Options options, Class<T> expectedResult) {
        Object result;
        try {
            result = org.asciidoctor.core.Asciidoctor.convert(content, prepareOptions(options));
        } catch (RuntimeException e) {
            throw coreException(e);
        }
        return adaptReturn(result, expectedResult);
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, Options options) throws IOException {
        Objects.requireNonNull(contentReader, "reader");
        Objects.requireNonNull(rendererWriter, "writer");
        StringBuilder content = new StringBuilder();
        char[] buffer = new char[8192];
        int length;
        while ((length = contentReader.read(buffer)) != -1) {
            content.append(buffer, 0, length);
        }
        String output = convert(content.toString(), options);
        if (output != null) {
            rendererWriter.write(output);
        }
        rendererWriter.flush();
    }

    @Override
    public String convertFile(File file, Options options) {
        return convertFile(file, options, String.class);
    }

    @Override
    public <T> T convertFile(File file, Options options, Class<T> expectedResult) {
        Object result;
        try {
            result = org.asciidoctor.core.Asciidoctor.convertFile(file.getPath(), prepareOptions(options));
        } catch (RuntimeException e) {
            throw coreException(e);
        }
        return adaptReturn(result, expectedResult);
    }

    @Override
    public String[] convertDirectory(Iterable<File> directoryWalker, Options options) {
        return convertAllFiles(directoryWalker, options);
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, Options options) {
        return convertAllFiles(asciidoctorFiles, options);
    }

    private String[] convertAllFiles(Iterable<File> files, Options options) {
        List<String> results = new ArrayList<>();
        for (File file : files) {
            String result = convertFile(file, options);
            if (result != null) {
                results.add(result);
            }
        }
        return results.toArray(new String[0]);
    }

    @Override
    public Document load(String content, Options options) {
        return NodeConverter.wrap(org.asciidoctor.core.Asciidoctor.load(content, prepareOptions(options)));
    }

    @Override
    public Document loadFile(File file, Options options) {
        return NodeConverter.wrap(org.asciidoctor.core.Asciidoctor.loadFile(file.getAbsolutePath(),
                prepareOptions(options)));
    }

    /**
     * @return the version of AsciidoctorJ (this implementation doesn't embed Asciidoctor Ruby)
     */
    @Override
    public String asciidoctorVersion() {
        return org.asciidoctor.core.Asciidoctor.VERSION;
    }

    @Override
    public void shutdown() {
        // nothing to do since there's no runtime to tear down
    }
}
