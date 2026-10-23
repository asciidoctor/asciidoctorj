package org.asciidoctor.core.api.extension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.AbstractNode;
import org.asciidoctor.core.Document;
import org.asciidoctor.core.PreprocessorReader;
import org.asciidoctor.core.Reader;
import org.asciidoctor.core.api.ast.NodeConverter;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.DefaultAttribute;
import org.asciidoctor.extension.DefaultAttributes;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.Format;
import org.asciidoctor.extension.FormatType;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Location;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.PositionalAttributes;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Processor;
import org.asciidoctor.extension.Treeprocessor;

/**
 * The processors of the implementation that proxy the processors of the AsciidoctorJ API (like the processor proxies
 * of AsciidoctorJ), and the helpers to configure and instantiate the processors of the API.
 */
final class ProcessorProxies {

    private ProcessorProxies() {
    }

    // --- configuration ---

    /**
     * Configures the processor of the API once it's registered: the configuration declared by the annotations of its
     * class (e.g., {@link Name} or {@link Contexts}) is merged with the configuration of the processor (which takes
     * precedence), then the configuration can no longer be replaced.
     */
    static void finalizeConfig(Processor processor) {
        Map<String, Object> config = annotationConfig(processor.getClass());
        config.putAll(processor.getConfig());
        ProcessorImpl delegate = processor.unwrap(ProcessorImpl.class);
        delegate.finalizeConfig(config);
    }

    /**
     * @return the configuration declared by the annotations of the processor class
     */
    static Map<String, Object> annotationConfig(Class<?> type) {
        Map<String, Object> config = new LinkedHashMap<>();
        Name name = type.getAnnotation(Name.class);
        if (name != null) {
            config.put("name", name.value());
        }
        Contexts contexts = type.getAnnotation(Contexts.class);
        if (contexts != null) {
            config.put(Contexts.KEY, new ArrayList<>(Arrays.asList(contexts.value())));
        }
        org.asciidoctor.ast.ContentModel contentModel = type.getAnnotation(org.asciidoctor.ast.ContentModel.class);
        if (contentModel != null) {
            config.put(org.asciidoctor.ast.ContentModel.KEY, contentModel.value());
        }
        PositionalAttributes positionalAttributes = type.getAnnotation(PositionalAttributes.class);
        if (positionalAttributes != null) {
            config.put("positional_attrs", new ArrayList<>(Arrays.asList(positionalAttributes.value())));
        }
        Map<String, Object> defaultAttrs = new LinkedHashMap<>();
        DefaultAttribute defaultAttribute = type.getAnnotation(DefaultAttribute.class);
        if (defaultAttribute != null) {
            defaultAttrs.put(defaultAttribute.key(), defaultAttribute.value());
        }
        DefaultAttributes defaultAttributes = type.getAnnotation(DefaultAttributes.class);
        if (defaultAttributes != null) {
            for (DefaultAttribute attribute : defaultAttributes.value()) {
                defaultAttrs.put(attribute.key(), attribute.value());
            }
        }
        if (!defaultAttrs.isEmpty()) {
            config.put("default_attrs", defaultAttrs);
        }
        Format format = type.getAnnotation(Format.class);
        if (format != null) {
            if (format.value() == FormatType.CUSTOM) {
                config.put(InlineMacroProcessor.REGEXP, format.regexp());
            } else {
                config.put("format", format.value().optionValue());
            }
        }
        Location location = type.getAnnotation(Location.class);
        if (location != null) {
            config.put("location", location.value().optionValue());
        }
        return config;
    }

    /**
     * @return the configuration of the processor of the implementation that corresponds to the configuration of the
     * processor of the API (where the values may be Ruby symbols, e.g., :paragraph)
     */
    static Map<String, Object> coreConfig(Map<String, Object> config) {
        Map<String, Object> result = new LinkedHashMap<>();
        config.forEach((key, value) -> result.put(key, ProcessorImpl.symbolValue(value)));
        Object contexts = result.get(Contexts.KEY);
        if (contexts != null) {
            Set<String> resolved = new LinkedHashSet<>();
            Collection<?> values = contexts instanceof Collection<?> collection ? collection
                    : contexts instanceof Object[] array ? Arrays.asList(array) : List.of(contexts);
            values.forEach(context -> resolved.add(ProcessorImpl.unsymbolize(String.valueOf(context))));
            result.put(Contexts.KEY, resolved);
        }
        Object positionalAttrs = result.get("positional_attrs");
        if (positionalAttrs instanceof Object[] array) {
            result.put("positional_attrs", new ArrayList<>(Arrays.asList(array)));
        }
        return result;
    }

    // --- instantiation ---

    /**
     * Instantiates the processor class with the constructor that accepts the most arguments among the specified
     * arguments (e.g., (String name, Map config), then (String name), then ()).
     */
    static <T> T instantiate(Class<T> type, Object... args) {
        Constructor<?> best = null;
        int bestCount = -1;
        for (Constructor<?> constructor : type.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length > args.length || parameterTypes.length <= bestCount) {
                continue;
            }
            boolean matches = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (args[i] != null && !parameterTypes[i].isInstance(args[i])) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                best = constructor;
                bestCount = parameterTypes.length;
            }
        }
        if (best == null) {
            throw new IllegalArgumentException("No suitable constructor found for " + type.getName());
        }
        try {
            return type.cast(best.newInstance(Arrays.copyOf(args, bestCount)));
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof RuntimeException runtimeException ? runtimeException
                    : new IllegalStateException(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to instantiate " + type.getName(), e);
        }
    }

    /**
     * @return the class with the specified name, loaded with the context class loader (or the class loader of this
     * class)
     */
    @SuppressWarnings("unchecked")
    static <T> Class<? extends T> loadClass(String className, Class<T> type) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<?> clazz = Class.forName(className, true,
                    classLoader != null ? classLoader : ProcessorProxies.class.getClassLoader());
            if (!type.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(className + " is not a " + type.getName());
            }
            return (Class<? extends T>) clazz;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + className, e);
        }
    }

    /**
     * @return the name of the processor: the explicit name, otherwise the name of the processor, otherwise the name
     * declared by the configuration
     */
    static String resolveName(String explicitName, String name, Map<String, Object> config) {
        if (explicitName != null) {
            return explicitName;
        } else if (name != null) {
            return name;
        }
        Object configName = config.get("name");
        return configName == null ? null : configName.toString();
    }

    // --- proxies ---

    static final class PreprocessorProxy extends org.asciidoctor.core.extension.Preprocessor {
        private final Preprocessor processor;

        PreprocessorProxy(Preprocessor processor) {
            super(coreConfig(processor.getConfig()));
            this.processor = processor;
        }

        @Override
        public Reader process(Document document, PreprocessorReader reader) {
            PreprocessorReaderImpl readerArg = new PreprocessorReaderImpl(reader);
            org.asciidoctor.extension.Reader result = processor.process(NodeConverter.wrap(document), readerArg);
            return result == null || result == readerArg ? null : ReaderImpl.unwrap(result);
        }
    }

    static final class TreeprocessorProxy extends org.asciidoctor.core.extension.TreeProcessor {
        private final Treeprocessor processor;

        TreeprocessorProxy(Treeprocessor processor) {
            super(coreConfig(processor.getConfig()));
            this.processor = processor;
        }

        @Override
        public Document process(Document document) {
            return NodeConverter.unwrap(processor.process(NodeConverter.wrap(document)));
        }
    }

    static final class PostprocessorProxy extends org.asciidoctor.core.extension.Postprocessor {
        private final Postprocessor processor;

        PostprocessorProxy(Postprocessor processor) {
            super(coreConfig(processor.getConfig()));
            this.processor = processor;
        }

        @Override
        public String process(Document document, String output) {
            return processor.process(NodeConverter.wrap(document), output);
        }
    }

    static final class IncludeProcessorProxy extends org.asciidoctor.core.extension.IncludeProcessor {
        private final IncludeProcessor processor;

        IncludeProcessorProxy(IncludeProcessor processor) {
            super(coreConfig(processor.getConfig()));
            this.processor = processor;
        }

        @Override
        public boolean handles(Document doc, String target) {
            return processor.handles(target);
        }

        @Override
        public boolean handles(String target) {
            return processor.handles(target);
        }

        @Override
        public void process(Document document, PreprocessorReader reader, String target,
                Map<String, Object> attributes) {
            processor.process(NodeConverter.wrap(document), new PreprocessorReaderImpl(reader), target, attributes);
        }
    }

    static final class DocinfoProcessorProxy extends org.asciidoctor.core.extension.DocinfoProcessor {
        private final DocinfoProcessor processor;

        DocinfoProcessorProxy(DocinfoProcessor processor) {
            super(coreConfig(processor.getConfig()));
            this.processor = processor;
        }

        @Override
        public String process(Document document) {
            return processor.process(NodeConverter.wrap(document));
        }
    }

    static final class BlockProcessorProxy extends org.asciidoctor.core.extension.BlockProcessor {
        private final BlockProcessor processor;

        BlockProcessorProxy(BlockProcessor processor, String name) {
            super(name, coreConfig(processor.getConfig()));
            this.processor = processor;
        }

        @Override
        public AbstractBlock process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
            Object result = processor.process(NodeConverter.wrap(parent), new ReaderImpl(reader), attributes);
            return toBlock(result);
        }
    }

    static final class BlockMacroProcessorProxy extends org.asciidoctor.core.extension.BlockMacroProcessor {
        private final BlockMacroProcessor processor;

        BlockMacroProcessorProxy(BlockMacroProcessor processor, String name) {
            super(name, coreConfig(processor.getConfig()));
            this.processor = processor;
        }

        @Override
        public AbstractBlock process(AbstractBlock parent, String target, Map<String, Object> attributes) {
            return toBlock(processor.process(NodeConverter.wrap(parent), target, attributes));
        }
    }

    static final class InlineMacroProcessorProxy extends org.asciidoctor.core.extension.InlineMacroProcessor {
        private final InlineMacroProcessor processor;

        InlineMacroProcessorProxy(InlineMacroProcessor processor, String name) {
            super(name, coreConfig(processor.getConfig()));
            this.processor = processor;
        }

        @Override
        public Object process(AbstractNode parent, String target, Map<String, Object> attributes) {
            ContentNode parentNode = NodeConverter.wrap(parent);
            Object result = processor.process((StructuralNode) parentNode, target, attributes);
            return NodeConverter.fromApi(result);
        }
    }

    private static AbstractBlock toBlock(Object result) {
        Object block = NodeConverter.fromApi(result);
        if (block == null || block instanceof AbstractBlock) {
            return (AbstractBlock) block;
        }
        throw new IllegalArgumentException("The processor must return a StructuralNode (or null), not a "
                + block.getClass().getName());
    }

    /**
     * @return a new mutable configuration (for the constructors of the processors)
     */
    static Map<String, Object> newConfig() {
        return new HashMap<>();
    }
}
