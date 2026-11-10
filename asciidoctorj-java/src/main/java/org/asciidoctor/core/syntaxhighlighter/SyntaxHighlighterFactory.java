package org.asciidoctor.core.syntaxhighlighter;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A registry for syntax highlighter implementations.
 *
 * <p>A syntax highlighter may be registered as a SyntaxHighlighter class (which must have a constructor that accepts
 * the name, backend and options Map, a constructor that accepts the name, or a no-arg constructor), a
 * {@link SyntaxHighlighter.Constructor} or a SyntaxHighlighter instance.
 */
public abstract class SyntaxHighlighterFactory {

    /**
     * @return the global registry of syntax highlighters, which includes the built-in adapters.
     */
    public static SyntaxHighlighterFactory getDefault() {
        return DefaultFactory.INSTANCE;
    }

    /**
     * @return a new custom factory (which does not consult the default registry) seeded with the specified registry
     */
    public static SyntaxHighlighterFactory custom(Map<String, ?> seedRegistry) {
        return new CustomFactory(seedRegistry);
    }

    /**
     * @return a new factory seeded with the specified registry that falls back to the default registry
     */
    public static SyntaxHighlighterFactory proxy(Map<String, ?> seedRegistry) {
        return new DefaultFactoryProxy(seedRegistry);
    }

    protected abstract Map<String, Optional<Object>> registry();

    /**
     * Associates the syntax highlighter class, constructor or object with the specified names.
     *
     * @param syntaxHighlighter the syntax highlighter implementation to register
     * @param names one or more String names with which to register this syntax highlighter implementation.
     */
    public synchronized void register(Object syntaxHighlighter, String... names) {
        for (String name : names) {
            registry().put(name, Optional.ofNullable(syntaxHighlighter));
        }
    }

    /**
     * Retrieves the syntax highlighter class, constructor or object registered for the specified name.
     *
     * @param name The String name of the syntax highlighter to retrieve.
     * @return the SyntaxHighlighter class, constructor or object registered for this name, or null.
     */
    public Object forName(String name) {
        Optional<Object> entry = registry().get(name);
        return entry == null ? null : entry.orElse(null);
    }

    /**
     * Resolves the name to a syntax highlighter instance, if found in the registry.
     *
     * @param name The String name of the syntax highlighter to create.
     * @param backend The String name of the backend for which this syntax highlighter is being used (default:
     *            'html5').
     * @param opts A Map of options providing information about the context in which this syntax highlighter is used:
     *            <ul>
     *            <li>document - The Document for which this syntax highlighter was created.</li>
     *            </ul>
     * @return a SyntaxHighlighter instance for the specified name, or null if not found.
     */
    public SyntaxHighlighter create(String name, String backend, Map<String, Object> opts) {
        Object syntaxHl = forName(name);
        if (syntaxHl == null) {
            return null;
        }
        SyntaxHighlighter instance = instantiate(syntaxHl, name, backend == null ? "html5" : backend,
                opts == null ? Map.of() : opts);
        if (instance.getName() == null) {
            throw new IllegalStateException(instance.getClass().getName() + " must specify a value for `name'");
        }
        return instance;
    }

    private static SyntaxHighlighter instantiate(Object syntaxHl, String name, String backend,
            Map<String, Object> opts) {
        if (syntaxHl instanceof SyntaxHighlighter instance) {
            return instance;
        } else if (syntaxHl instanceof SyntaxHighlighter.Constructor constructor) {
            return constructor.create(name, backend, opts);
        } else if (syntaxHl instanceof Class<?> clazz) {
            try {
                try {
                    return (SyntaxHighlighter) clazz.getConstructor(String.class, String.class, Map.class)
                            .newInstance(name, backend, opts);
                } catch (NoSuchMethodException e) {
                    try {
                        return (SyntaxHighlighter) clazz.getConstructor(String.class).newInstance(name);
                    } catch (NoSuchMethodException e2) {
                        return (SyntaxHighlighter) clazz.getConstructor().newInstance();
                    }
                }
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new IllegalStateException("Could not instantiate syntax highlighter: " + clazz.getName(),
                        e.getCause());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not instantiate syntax highlighter: " + clazz.getName(), e);
            }
        }
        throw new IllegalArgumentException("Invalid syntax highlighter: " + syntaxHl);
    }

    /**
     * A factory that has its own registry.
     */
    public static class CustomFactory extends SyntaxHighlighterFactory {
        private final Map<String, Optional<Object>> registry = new LinkedHashMap<>();

        public CustomFactory(Map<String, ?> seedRegistry) {
            if (seedRegistry != null) {
                seedRegistry.forEach((name, value) -> registry.put(name, Optional.ofNullable(value)));
            }
        }

        @Override
        protected Map<String, Optional<Object>> registry() {
            return registry;
        }
    }

    /**
     * The global registry of syntax highlighters. Registration is synchronized and is thus guaranteed to be thread
     * safe.
     */
    public static final class DefaultFactory extends SyntaxHighlighterFactory {
        static final DefaultFactory INSTANCE = new DefaultFactory();

        private static final Map<String, Supplier<Object>> PROVIDED = Map.of(
                "highlightjs", () -> (SyntaxHighlighter.Constructor) HighlightJsAdapter::new,
                "highlight.js", () -> (SyntaxHighlighter.Constructor) HighlightJsAdapter::new,
                "html-pipeline", () -> (SyntaxHighlighter.Constructor) HtmlPipelineAdapter::new,
                "prettify", () -> (SyntaxHighlighter.Constructor) PrettifyAdapter::new);

        private final Map<String, Optional<Object>> registry = new ConcurrentHashMap<>();

        private DefaultFactory() {
        }

        @Override
        protected Map<String, Optional<Object>> registry() {
            return registry;
        }

        /**
         * This method will lazily register the built-in implementations.
         */
        @Override
        public Object forName(String name) {
            Optional<Object> entry = registry.get(name);
            if (entry == null) {
                synchronized (this) {
                    entry = registry.computeIfAbsent(name, key -> {
                        Supplier<Object> provided = PROVIDED.get(key);
                        return Optional.ofNullable(provided == null ? null : provided.get());
                    });
                }
            }
            return entry.orElse(null);
        }
    }

    /**
     * A factory with its own registry that falls back to the default (global) registry.
     */
    public static class DefaultFactoryProxy extends CustomFactory {
        public DefaultFactoryProxy(Map<String, ?> seedRegistry) {
            super(seedRegistry);
        }

        @Override
        public Object forName(String name) {
            Optional<Object> entry = registry().get(name);
            return entry != null ? entry.orElse(null) : DefaultFactory.INSTANCE.forName(name);
        }
    }
}
