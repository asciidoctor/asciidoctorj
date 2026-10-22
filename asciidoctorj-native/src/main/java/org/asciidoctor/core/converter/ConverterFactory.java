package org.asciidoctor.core.converter;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A reusable registry for registering and instantiating {@link Converter} classes used to convert an AbstractNode to
 * an output (aka backend) format such as HTML or DocBook.
 *
 * <p>Converter objects are instantiated by passing a String backend name and, optionally, an options Map to the
 * {@link #create(String, Map)} method. The backend can be thought of as an intent to convert a document to a specified
 * format.
 *
 * <p>Applications interact with the factory either through the global, static registry exposed by
 * {@link #getDefault()} (and the static methods of {@link Converter}) or a custom factory created using
 * {@link #custom(Map)} or {@link #proxy(Map)}.
 *
 * <p>A converter may be registered as a Converter class (which must have a constructor that accepts the backend name
 * and an options Map, a constructor that accepts the backend name, or a no-arg constructor), a
 * {@link Converter.Constructor} or a Converter instance.
 */
public abstract class ConverterFactory {

    /**
     * Create an instance of a factory, optionally seeded with the specified converters map. If proxyDefault is set,
     * entries in the proxy registry are preferred over matching entries from the default registry.
     *
     * @param converters An optional Map of converters to use in place of ones in the default registry. The keys are
     *            backend names and the values are converter classes, constructors or instances.
     * @param proxyDefault A Boolean indicating whether to proxy the default registry (optional, default: true).
     * @return a factory instance seeded with the optional converters map.
     */
    public static ConverterFactory create(Map<String, ?> converters, boolean proxyDefault) {
        return proxyDefault ? proxy(converters) : custom(converters);
    }

    /**
     * @return a new custom factory (which does not consult the default registry) seeded with the specified converters
     */
    public static ConverterFactory custom(Map<String, ?> converters) {
        return new CustomFactory(converters);
    }

    /**
     * @return a new factory seeded with the specified converters that falls back to the default registry
     */
    public static ConverterFactory proxy(Map<String, ?> converters) {
        return new DefaultFactoryProxy(converters);
    }

    /**
     * @return the global registry of converters, which includes the built-in converters for HTML 5, DocBook 5 and
     *         man(ual) page, as well as any custom converters that have been explicitly registered.
     */
    public static ConverterFactory getDefault() {
        return DefaultFactory.INSTANCE;
    }

    /**
     * @return the registry of this factory
     */
    protected abstract Map<String, Object> registry();

    /**
     * @return the converter that handles any backend for which a converter is not registered, or null
     */
    protected abstract Object catchAll();

    protected abstract void setCatchAll(Object converter);

    /**
     * Register a custom converter with this factory to handle conversion for the specified backends. If the backend is
     * an asterisk (i.e., *), the converter will handle any backend for which a converter is not registered.
     *
     * @param converter The Converter class, constructor or instance to register.
     * @param backends One or more String backend names that this converter should be registered to handle.
     */
    public synchronized void register(Object converter, String... backends) {
        for (String backend : backends) {
            if (backend.equals("*")) {
                setCatchAll(converter);
            } else {
                registry().put(backend, converter);
            }
        }
    }

    /**
     * Lookup the custom converter registered with this factory to handle the specified backend.
     *
     * @param backend The String backend name.
     * @return the Converter class, constructor or instance registered to convert the specified backend or null if no
     *         match is found.
     */
    public Object forBackend(String backend) {
        Object converter = registry().get(backend);
        return converter != null ? converter : catchAll();
    }

    /**
     * Create a new Converter object that can be used to convert AbstractNodes to the format associated with the
     * backend. This method accepts an optional Map of options that are passed to the converter's constructor.
     *
     * <p>If a custom Converter is found to convert the specified backend, it's instantiated (if necessary) and returned
     * immediately. If a custom Converter is not found, an attempt is made to find a built-in converter.
     *
     * @param backend the String backend name.
     * @param opts a Map of options to customize creation; also passed to the converter's constructor
     * @return the Converter instance, or null if no converter is found.
     */
    public Converter<?> create(String backend, Map<String, Object> opts) {
        Object converter = forBackend(backend);
        if (converter == null) {
            return null;
        }
        return instantiate(converter, backend, opts == null ? Map.of() : opts);
    }

    /**
     * @return a copy of the Map of converters keyed by backend name. Intended for testing only.
     */
    public Map<String, Object> getConverters() {
        return new LinkedHashMap<>(registry());
    }

    /**
     * Unregister all converters that are registered with this factory. Intended for testing only.
     */
    public synchronized void unregisterAll() {
        registry().clear();
        setCatchAll(null);
    }

    /**
     * Instantiates the converter if it's a class or a constructor.
     */
    protected static Converter<?> instantiate(Object converter, String backend, Map<String, Object> opts) {
        if (converter instanceof Converter<?> instance) {
            return instance;
        } else if (converter instanceof Converter.Constructor constructor) {
            return constructor.create(backend, opts);
        } else if (converter instanceof Class<?> clazz) {
            try {
                try {
                    return (Converter<?>) clazz.getConstructor(String.class, Map.class).newInstance(backend, opts);
                } catch (NoSuchMethodException e) {
                    try {
                        return (Converter<?>) clazz.getConstructor(String.class).newInstance(backend);
                    } catch (NoSuchMethodException e2) {
                        return (Converter<?>) clazz.getConstructor().newInstance();
                    }
                }
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new IllegalStateException("Could not instantiate converter: " + clazz.getName(), e.getCause());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not instantiate converter: " + clazz.getName(), e);
            }
        }
        throw new IllegalArgumentException("Invalid converter: " + converter);
    }

    /**
     * A factory that has its own registry.
     */
    public static class CustomFactory extends ConverterFactory {
        private final Map<String, Object> registry = new LinkedHashMap<>();
        private Object catchAll;

        public CustomFactory(Map<String, ?> seedRegistry) {
            if (seedRegistry != null) {
                seedRegistry.forEach((backend, converter) -> {
                    if (backend.equals("*")) {
                        catchAll = converter;
                    } else {
                        registry.put(backend, converter);
                    }
                });
            }
        }

        public CustomFactory() {
            this(null);
        }

        @Override
        protected Map<String, Object> registry() {
            return registry;
        }

        @Override
        protected Object catchAll() {
            return catchAll;
        }

        @Override
        protected void setCatchAll(Object converter) {
            catchAll = converter;
        }
    }

    /**
     * The global registry of converters that are registered statically.
     *
     * <p>This registry includes built-in converters for HTML 5, DocBook 5 and man(ual) page, as well as any custom
     * converters that have been explicitly registered. Converter registration is synchronized and is thus guaranteed to
     * be thread safe.
     */
    public static final class DefaultFactory extends ConverterFactory {
        static final DefaultFactory INSTANCE = new DefaultFactory();

        private static final Map<String, Supplier<Converter.Constructor>> PROVIDED = Map.of(
                "docbook5", () -> DocBook5Converter::new,
                "html5", () -> Html5Converter::new,
                "manpage", () -> ManPageConverter::new);

        private final Map<String, Object> registry = new java.util.concurrent.ConcurrentHashMap<>();
        private volatile Object catchAll;

        private DefaultFactory() {
        }

        @Override
        protected Map<String, Object> registry() {
            return registry;
        }

        @Override
        protected Object catchAll() {
            return catchAll;
        }

        @Override
        protected void setCatchAll(Object converter) {
            catchAll = converter;
        }

        @Override
        public Object forBackend(String backend) {
            Object converter = registry.get(backend);
            if (converter != null) {
                return converter;
            }
            Supplier<Converter.Constructor> provided = PROVIDED.get(backend);
            if (provided != null) {
                synchronized (this) {
                    return registry.computeIfAbsent(backend, key -> provided.get());
                }
            }
            return catchAll;
        }

        @Override
        public synchronized void unregisterAll() {
            registry.keySet().removeIf(backend -> !PROVIDED.containsKey(backend));
            for (Map.Entry<String, Supplier<Converter.Constructor>> entry : PROVIDED.entrySet()) {
                registry.put(entry.getKey(), entry.getValue().get());
            }
            catchAll = null;
        }

        /**
         * @return whether the specified converter is the built-in converter for the backend
         */
        public static boolean isProvided(String backend) {
            return PROVIDED.containsKey(backend);
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
        public Object forBackend(String backend) {
            // NOTE a converter registered in this registry takes precedence, even if it's null
            if (registry().containsKey(backend)) {
                return registry().get(backend);
            }
            Object converter = DefaultFactory.INSTANCE.registry().get(backend);
            if (converter != null) {
                return converter;
            }
            if (DefaultFactory.isProvided(backend)) {
                return DefaultFactory.INSTANCE.forBackend(backend);
            }
            Object localCatchAll = catchAll();
            return localCatchAll != null ? localCatchAll : DefaultFactory.INSTANCE.catchAll();
        }
    }
}
