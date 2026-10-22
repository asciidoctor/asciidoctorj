package org.asciidoctor.core.extension;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.asciidoctor.core.Document;
import org.asciidoctor.core.Helpers;
import org.asciidoctor.core.Strings;

/**
 * The primary entry point into the extension system.
 *
 * <p>Registry holds the extensions which have been registered and activated, has methods for registering or defining
 * a processor and looks up extensions stored in the registry during parsing.
 *
 * <p>A processor may be registered as an instance, as a Processor class (or the String name of a Processor class) or
 * using a configurer (DSL), which receives a new instance of the processor to configure. For example:
 *
 * <pre>{@code
 * registry.preprocessor(FrontMatterPreprocessor.class);
 * registry.preprocessor(new FrontMatterPreprocessor());
 * registry.preprocessor(p -> p.process((doc, reader) -> reader));
 * registry.block("shout", b -> b.process((parent, reader, attrs) -> ...));
 * }</pre>
 */
public class Registry {
    private Document document;
    private final Map<String, Object> groups;

    private List<ProcessorExtension<Preprocessor>> preprocessorExtensions;
    private List<ProcessorExtension<TreeProcessor>> treeProcessorExtensions;
    private List<ProcessorExtension<Postprocessor>> postprocessorExtensions;
    private List<ProcessorExtension<IncludeProcessor>> includeProcessorExtensions;
    private List<ProcessorExtension<DocinfoProcessor>> docinfoProcessorExtensions;
    private Map<String, ProcessorExtension<BlockProcessor>> blockExtensions;
    private Map<String, ProcessorExtension<BlockMacroProcessor>> blockMacroExtensions;
    private Map<String, ProcessorExtension<InlineMacroProcessor>> inlineMacroExtensions;

    public Registry(Map<String, Object> groups) {
        this.groups = groups == null ? new LinkedHashMap<>() : groups;
        reset();
    }

    public Registry() {
        this(null);
    }

    /**
     * @return the Document on which the extensions in this registry are being used.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * @return the Map of Groups (instances or classes) that have been registered with this registry.
     */
    public Map<String, Object> getGroups() {
        return groups;
    }

    /**
     * Activates all the global extension Groups and the extension Groups associated with this registry.
     *
     * @param document the Document on which the extensions are to be used.
     * @return the instance of this Registry.
     */
    public Registry activate(Document document) {
        if (this.document != null) {
            reset();
        }
        this.document = document;
        List<Object> extGroups = new ArrayList<>(Extensions.getGroups().values());
        extGroups.addAll(groups.values());
        for (Object group : extGroups) {
            if (group instanceof Group g) {
                g.activate(this);
            } else if (group instanceof Consumer<?> consumer) {
                @SuppressWarnings("unchecked")
                Consumer<Registry> registryConsumer = (Consumer<Registry>) consumer;
                registryConsumer.accept(this);
            } else if (group instanceof Class<?> clazz) {
                ((Group) instantiate(clazz)).activate(this);
            } else {
                throw new IllegalArgumentException("Invalid extension group: " + group);
            }
        }
        return this;
    }

    // --- preprocessor ---

    /**
     * Registers a {@link Preprocessor} with the extension registry to process the AsciiDoc source before parsing
     * begins.
     *
     * <p>The Preprocessor may be a Preprocessor subclass, an instance of a Preprocessor subclass or the String name of a
     * Preprocessor subclass.
     *
     * @return the Extension stored in the registry that proxies the instance of this Preprocessor.
     */
    public ProcessorExtension<Preprocessor> preprocessor(Object processor, Map<String, Object> config) {
        if (preprocessorExtensions == null) {
            preprocessorExtensions = new ArrayList<>();
        }
        return addDocumentProcessor("preprocessor", Preprocessor.class, processor, config, preprocessorExtensions);
    }

    public ProcessorExtension<Preprocessor> preprocessor(Object processor) {
        return preprocessor(processor, null);
    }

    /**
     * Registers a {@link Preprocessor} defined using the DSL.
     */
    public ProcessorExtension<Preprocessor> preprocessor(Consumer<Preprocessor> configurer) {
        Preprocessor processor = new Preprocessor();
        configurer.accept(processor);
        requireProcessBlock("preprocessor", processor.isProcessBlockGiven());
        return preprocessor(processor);
    }

    /**
     * @return whether any {@link Preprocessor} extensions have been registered.
     */
    public boolean hasPreprocessors() {
        return preprocessorExtensions != null;
    }

    /**
     * @return the Extension proxy objects for all Preprocessor instances in this registry.
     */
    public List<ProcessorExtension<Preprocessor>> getPreprocessors() {
        return preprocessorExtensions;
    }

    // --- tree processor ---

    /**
     * Registers a {@link TreeProcessor} with the extension registry to process the AsciiDoc source after parsing is
     * complete.
     *
     * @return the Extension stored in the registry that proxies the instance of this TreeProcessor.
     */
    public ProcessorExtension<TreeProcessor> treeProcessor(Object processor, Map<String, Object> config) {
        if (treeProcessorExtensions == null) {
            treeProcessorExtensions = new ArrayList<>();
        }
        return addDocumentProcessor("tree_processor", TreeProcessor.class, processor, config, treeProcessorExtensions);
    }

    public ProcessorExtension<TreeProcessor> treeProcessor(Object processor) {
        return treeProcessor(processor, null);
    }

    /**
     * Registers a {@link TreeProcessor} defined using the DSL.
     */
    public ProcessorExtension<TreeProcessor> treeProcessor(Consumer<TreeProcessor> configurer) {
        TreeProcessor processor = new TreeProcessor();
        configurer.accept(processor);
        requireProcessBlock("tree processor", processor.isProcessBlockGiven());
        return treeProcessor(processor);
    }

    /**
     * Alias of {@link #treeProcessor(Object)} (the deprecated name in Ruby and the name used by AsciidoctorJ).
     */
    public ProcessorExtension<TreeProcessor> treeprocessor(Object processor) {
        return treeProcessor(processor);
    }

    public ProcessorExtension<TreeProcessor> treeprocessor(Consumer<TreeProcessor> configurer) {
        return treeProcessor(configurer);
    }

    public boolean hasTreeProcessors() {
        return treeProcessorExtensions != null;
    }

    public List<ProcessorExtension<TreeProcessor>> getTreeProcessors() {
        return treeProcessorExtensions;
    }

    // --- postprocessor ---

    /**
     * Registers a {@link Postprocessor} with the extension registry to process the output after conversion is
     * complete.
     *
     * @return the Extension stored in the registry that proxies the instance of this Postprocessor.
     */
    public ProcessorExtension<Postprocessor> postprocessor(Object processor, Map<String, Object> config) {
        if (postprocessorExtensions == null) {
            postprocessorExtensions = new ArrayList<>();
        }
        return addDocumentProcessor("postprocessor", Postprocessor.class, processor, config, postprocessorExtensions);
    }

    public ProcessorExtension<Postprocessor> postprocessor(Object processor) {
        return postprocessor(processor, null);
    }

    /**
     * Registers a {@link Postprocessor} defined using the DSL.
     */
    public ProcessorExtension<Postprocessor> postprocessor(Consumer<Postprocessor> configurer) {
        Postprocessor processor = new Postprocessor();
        configurer.accept(processor);
        requireProcessBlock("postprocessor", processor.isProcessBlockGiven());
        return postprocessor(processor);
    }

    public boolean hasPostprocessors() {
        return postprocessorExtensions != null;
    }

    public List<ProcessorExtension<Postprocessor>> getPostprocessors() {
        return postprocessorExtensions;
    }

    // --- include processor ---

    /**
     * Registers an {@link IncludeProcessor} with the extension registry to have a shot at handling the include
     * directive.
     *
     * @return the Extension stored in the registry that proxies the instance of this IncludeProcessor.
     */
    public ProcessorExtension<IncludeProcessor> includeProcessor(Object processor, Map<String, Object> config) {
        if (includeProcessorExtensions == null) {
            includeProcessorExtensions = new ArrayList<>();
        }
        return addDocumentProcessor("include_processor", IncludeProcessor.class, processor, config, includeProcessorExtensions);
    }

    public ProcessorExtension<IncludeProcessor> includeProcessor(Object processor) {
        return includeProcessor(processor, null);
    }

    /**
     * Registers an {@link IncludeProcessor} defined using the DSL.
     */
    public ProcessorExtension<IncludeProcessor> includeProcessor(Consumer<IncludeProcessor> configurer) {
        IncludeProcessor processor = new IncludeProcessor();
        configurer.accept(processor);
        requireProcessBlock("include processor", processor.isProcessBlockGiven());
        return includeProcessor(processor);
    }

    public boolean hasIncludeProcessors() {
        return includeProcessorExtensions != null;
    }

    public List<ProcessorExtension<IncludeProcessor>> getIncludeProcessors() {
        return includeProcessorExtensions;
    }

    // --- docinfo processor ---

    /**
     * Registers a {@link DocinfoProcessor} with the extension registry to add additional docinfo to the document.
     *
     * @return the Extension stored in the registry that proxies the instance of this DocinfoProcessor.
     */
    public ProcessorExtension<DocinfoProcessor> docinfoProcessor(Object processor, Map<String, Object> config) {
        if (docinfoProcessorExtensions == null) {
            docinfoProcessorExtensions = new ArrayList<>();
        }
        return addDocumentProcessor("docinfo_processor", DocinfoProcessor.class, processor, config, docinfoProcessorExtensions);
    }

    public ProcessorExtension<DocinfoProcessor> docinfoProcessor(Object processor) {
        return docinfoProcessor(processor, null);
    }

    /**
     * Registers a {@link DocinfoProcessor} defined using the DSL.
     */
    public ProcessorExtension<DocinfoProcessor> docinfoProcessor(Consumer<DocinfoProcessor> configurer) {
        DocinfoProcessor processor = new DocinfoProcessor();
        configurer.accept(processor);
        requireProcessBlock("docinfo processor", processor.isProcessBlockGiven());
        return docinfoProcessor(processor);
    }

    /**
     * Checks whether any {@link DocinfoProcessor} extensions have been registered.
     *
     * @param location A String for selecting docinfo extensions at a given location (head or footer) (default: null)
     * @return a Boolean indicating whether any DocinfoProcessor extensions are registered.
     */
    public boolean hasDocinfoProcessors(String location) {
        if (docinfoProcessorExtensions == null) {
            return false;
        } else if (location == null) {
            return true;
        }
        return docinfoProcessorExtensions.stream().anyMatch(ext -> location.equals(String.valueOf(
                ext.getConfig().get("location"))));
    }

    public boolean hasDocinfoProcessors() {
        return hasDocinfoProcessors(null);
    }

    /**
     * Retrieves the Extension proxy objects for all the DocinfoProcessor instances stored in this registry.
     *
     * @param location A String for selecting docinfo extensions at a given location (head or footer) (default: null)
     * @return a List of Extension proxy objects.
     */
    public List<ProcessorExtension<DocinfoProcessor>> getDocinfoProcessors(String location) {
        if (docinfoProcessorExtensions == null) {
            return new ArrayList<>();
        } else if (location == null) {
            return docinfoProcessorExtensions;
        }
        return docinfoProcessorExtensions.stream().filter(ext -> location.equals(String.valueOf(
                ext.getConfig().get("location")))).toList();
    }

    public List<ProcessorExtension<DocinfoProcessor>> getDocinfoProcessors() {
        return getDocinfoProcessors(null);
    }

    // --- block ---

    /**
     * Registers a {@link BlockProcessor} with the extension registry to process the block content (i.e., delimited
     * block or paragraph) in the AsciiDoc source annotated with the specified block name (i.e., style).
     *
     * <p>The BlockProcessor may be a BlockProcessor subclass, an instance of a BlockProcessor subclass or the String
     * name of a BlockProcessor subclass. The second argument is the name of the AsciiDoc block content (i.e.,
     * delimited block or paragraph) that this processor is registered to handle. If a block name is not passed as an
     * argument, it gets read from the name property of the BlockProcessor instance. If a name still cannot be
     * determined, an error is raised.
     *
     * @return an instance of the Extension proxy object that is stored in the registry and manages the instance of
     *         this BlockProcessor.
     */
    public ProcessorExtension<BlockProcessor> block(Object processor, String name, Map<String, Object> config) {
        if (blockExtensions == null) {
            blockExtensions = new LinkedHashMap<>();
        }
        return addSyntaxProcessor("block", BlockProcessor.class, processor, name, config, blockExtensions);
    }

    public ProcessorExtension<BlockProcessor> block(Object processor, String name) {
        return block(processor, name, null);
    }

    public ProcessorExtension<BlockProcessor> block(Object processor) {
        return block(processor, null, null);
    }

    /**
     * Registers a {@link BlockProcessor} defined using the DSL.
     */
    public ProcessorExtension<BlockProcessor> block(String name, Consumer<BlockProcessor> configurer) {
        BlockProcessor processor = new BlockProcessor(name);
        configurer.accept(processor);
        requireName("block", processor.getName());
        requireProcessBlock("block", processor.isProcessBlockGiven());
        return block(processor);
    }

    public ProcessorExtension<BlockProcessor> block(Consumer<BlockProcessor> configurer) {
        return block((String) null, configurer);
    }

    /**
     * @return whether any {@link BlockProcessor} extensions have been registered.
     */
    public boolean hasBlocks() {
        return blockExtensions != null;
    }

    /**
     * Checks whether any {@link BlockProcessor} extensions are registered to handle the specified block name appearing
     * on the specified context.
     *
     * @return the Extension proxy object for the BlockProcessor that matches the block name and context or null if no
     *         match is found.
     */
    public ProcessorExtension<BlockProcessor> isRegisteredForBlock(String name, String context) {
        ProcessorExtension<BlockProcessor> ext = blockExtensions == null ? null : blockExtensions.get(name);
        return ext != null && ext.getInstance().getContexts().contains(context) ? ext : null;
    }

    /**
     * Retrieves the Extension proxy object for the BlockProcessor registered to handle block content with the name.
     *
     * @param name the String block name
     * @return the Extension object stored in the registry that proxies the corresponding BlockProcessor or null if a
     *         match is not found.
     */
    public ProcessorExtension<BlockProcessor> findBlockExtension(String name) {
        return blockExtensions == null ? null : blockExtensions.get(name);
    }

    // --- block macro ---

    /**
     * Registers a {@link BlockMacroProcessor} with the extension registry to process a block macro with the specified
     * name.
     *
     * @return an instance of the Extension proxy object that is stored in the registry and manages the instance of
     *         this BlockMacroProcessor.
     */
    public ProcessorExtension<BlockMacroProcessor> blockMacro(Object processor, String name,
            Map<String, Object> config) {
        if (blockMacroExtensions == null) {
            blockMacroExtensions = new LinkedHashMap<>();
        }
        return addSyntaxProcessor("block_macro", BlockMacroProcessor.class, processor, name, config,
                blockMacroExtensions);
    }

    public ProcessorExtension<BlockMacroProcessor> blockMacro(Object processor, String name) {
        return blockMacro(processor, name, null);
    }

    public ProcessorExtension<BlockMacroProcessor> blockMacro(Object processor) {
        return blockMacro(processor, null, null);
    }

    /**
     * Registers a {@link BlockMacroProcessor} defined using the DSL.
     */
    public ProcessorExtension<BlockMacroProcessor> blockMacro(String name,
            Consumer<BlockMacroProcessor> configurer) {
        BlockMacroProcessor processor = new BlockMacroProcessor(name);
        configurer.accept(processor);
        requireName("block macro", processor.name);
        requireProcessBlock("block macro", processor.isProcessBlockGiven());
        return blockMacro(processor);
    }

    public ProcessorExtension<BlockMacroProcessor> blockMacro(Consumer<BlockMacroProcessor> configurer) {
        return blockMacro((String) null, configurer);
    }

    public boolean hasBlockMacros() {
        return blockMacroExtensions != null;
    }

    /**
     * Checks whether any {@link BlockMacroProcessor} extensions are registered to handle the block macro with the
     * specified name.
     *
     * @return the Extension proxy object for the BlockMacroProcessor that matches the macro name or null if no match
     *         is found.
     */
    // TODO only allow blank target if format is short
    public ProcessorExtension<BlockMacroProcessor> isRegisteredForBlockMacro(String name) {
        return blockMacroExtensions == null ? null : blockMacroExtensions.get(name);
    }

    public ProcessorExtension<BlockMacroProcessor> findBlockMacroExtension(String name) {
        return blockMacroExtensions == null ? null : blockMacroExtensions.get(name);
    }

    // --- inline macro ---

    /**
     * Registers an {@link InlineMacroProcessor} with the extension registry to process an inline macro with the
     * specified name.
     *
     * @return an instance of the Extension proxy object that is stored in the registry and manages the instance of
     *         this InlineMacroProcessor.
     */
    public ProcessorExtension<InlineMacroProcessor> inlineMacro(Object processor, String name,
            Map<String, Object> config) {
        if (inlineMacroExtensions == null) {
            inlineMacroExtensions = new LinkedHashMap<>();
        }
        return addSyntaxProcessor("inline_macro", InlineMacroProcessor.class, processor, name, config,
                inlineMacroExtensions);
    }

    public ProcessorExtension<InlineMacroProcessor> inlineMacro(Object processor, String name) {
        return inlineMacro(processor, name, null);
    }

    public ProcessorExtension<InlineMacroProcessor> inlineMacro(Object processor) {
        return inlineMacro(processor, null, null);
    }

    /**
     * Registers an {@link InlineMacroProcessor} defined using the DSL.
     */
    public ProcessorExtension<InlineMacroProcessor> inlineMacro(String name,
            Consumer<InlineMacroProcessor> configurer) {
        InlineMacroProcessor processor = new InlineMacroProcessor(name);
        configurer.accept(processor);
        requireName("inline macro", processor.getName());
        requireProcessBlock("inline macro", processor.isProcessBlockGiven());
        return inlineMacro(processor);
    }

    public ProcessorExtension<InlineMacroProcessor> inlineMacro(Consumer<InlineMacroProcessor> configurer) {
        return inlineMacro((String) null, configurer);
    }

    public boolean hasInlineMacros() {
        return inlineMacroExtensions != null;
    }

    /**
     * Checks whether any {@link InlineMacroProcessor} extensions are registered to handle the inline macro with the
     * specified name.
     *
     * @return the Extension proxy object for the InlineMacroProcessor that matches the macro name or null if no match
     *         is found.
     */
    public ProcessorExtension<InlineMacroProcessor> isRegisteredForInlineMacro(String name) {
        return inlineMacroExtensions == null ? null : inlineMacroExtensions.get(name);
    }

    public ProcessorExtension<InlineMacroProcessor> findInlineMacroExtension(String name) {
        return inlineMacroExtensions == null ? null : inlineMacroExtensions.get(name);
    }

    /**
     * @return the Extension proxy objects for all InlineMacroProcessor instances in this registry.
     */
    public List<ProcessorExtension<InlineMacroProcessor>> getInlineMacros() {
        return inlineMacroExtensions == null ? new ArrayList<>() : new ArrayList<>(inlineMacroExtensions.values());
    }

    /**
     * Inserts the document processor Extension instance as the first processor of its kind in the extension registry.
     *
     * @return the Extension stored in the registry that proxies the instance of this processor.
     */
    public <T extends Processor> ProcessorExtension<T> prefer(ProcessorExtension<T> extension) {
        List<?> store = switch (extension.getKind()) {
            case "preprocessor" -> preprocessorExtensions;
            case "tree_processor" -> treeProcessorExtensions;
            case "postprocessor" -> postprocessorExtensions;
            case "include_processor" -> includeProcessorExtensions;
            case "docinfo_processor" -> docinfoProcessorExtensions;
            default -> throw new IllegalArgumentException("Cannot prefer extension of kind: " + extension.getKind());
        };
        @SuppressWarnings("unchecked")
        List<ProcessorExtension<T>> typedStore = (List<ProcessorExtension<T>>) store;
        typedStore.remove(extension);
        typedStore.add(0, extension);
        return extension;
    }

    private static void requireProcessBlock(String kindName, boolean given) {
        if (!given) {
            throw new IllegalStateException("No block specified to process " + kindName + " extension");
        }
    }

    private static void requireName(String kindName, String name) {
        if (name == null) {
            throw new IllegalArgumentException("No name specified for " + kindName + " extension");
        }
    }

    private <T extends Processor> ProcessorExtension<T> addDocumentProcessor(String kind, Class<T> kindClass,
            Object processor, Map<String, Object> config, List<ProcessorExtension<T>> store) {
        String kindName = kind.replace('_', ' ');
        ProcessorExtension<T> extension;
        Class<?> processorClass = processor instanceof Class<?> || processor instanceof String
                ? Helpers.resolveClass(processor) : null;
        // style 2: specified as Class or String class name
        if (processorClass != null) {
            if (!kindClass.isAssignableFrom(processorClass)) {
                throw new IllegalArgumentException("Invalid type for " + kindName + " extension: " + processor);
            }
            T processorInstance = kindClass.cast(instantiate(processorClass, config));
            extension = new ProcessorExtension<>(kind, processorInstance);
        // style 3: specified as instance
        } else if (kindClass.isInstance(processor)) {
            T processorInstance = kindClass.cast(processor);
            processorInstance.updateConfig(config);
            extension = new ProcessorExtension<>(kind, processorInstance);
        } else {
            throw new IllegalArgumentException("Invalid arguments specified for registering " + kindName
                    + " extension: " + processor);
        }

        if (">>".equals(String.valueOf(extension.getConfig().get("position")))) {
            store.add(0, extension);
        } else {
            store.add(extension);
        }
        return extension;
    }

    private <T extends SyntaxProcessor> ProcessorExtension<T> addSyntaxProcessor(String kind, Class<T> kindClass,
            Object processor, String name, Map<String, Object> config, Map<String, ProcessorExtension<T>> store) {
        String kindName = kind.replace('_', ' ');
        Class<?> processorClass = processor instanceof Class<?> || processor instanceof String
                ? Helpers.resolveClass(processor) : null;
        T processorInstance;
        // style 2: specified as Class or String class name
        if (processorClass != null) {
            if (!kindClass.isAssignableFrom(processorClass)) {
                throw new IllegalArgumentException("Class specified for " + kindName
                        + " extension does not inherit from " + kindClass.getSimpleName() + ": " + processor);
            }
            processorInstance = kindClass.cast(instantiateSyntaxProcessor(processorClass, name, config));
            if (name == null && (name = processorInstance.name) == null) {
                throw new IllegalArgumentException("No name specified for " + kindName + " extension: " + processor);
            }
            if (processorInstance.name == null) {
                processorInstance.name = name;
            }
        // style 3: specified as instance
        } else if (kindClass.isInstance(processor)) {
            processorInstance = kindClass.cast(processor);
            processorInstance.updateConfig(config);
            // TODO need a test for this override!
            if (name != null) {
                processorInstance.name = name;
            } else if ((name = processorInstance.name) == null) {
                throw new IllegalArgumentException("No name specified for " + kindName + " extension: " + processor);
            }
        } else {
            throw new IllegalArgumentException("Invalid arguments specified for registering " + kindName
                    + " extension: " + processor);
        }
        // NOTE validates the name (if applicable)
        processorInstance.getName();
        ProcessorExtension<T> extension = new ProcessorExtension<>(kind, processorInstance);
        store.put(name, extension);
        return extension;
    }

    private static Object instantiate(Class<?> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (InvocationTargetException e) {
            throw rethrow(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Could not instantiate " + clazz.getName(), e);
        }
    }

    private static Object instantiate(Class<?> clazz, Map<String, Object> config) {
        try {
            try {
                return clazz.getConstructor(Map.class).newInstance(config == null ? new LinkedHashMap<>() : config);
            } catch (NoSuchMethodException e) {
                Object instance = clazz.getConstructor().newInstance();
                if (instance instanceof Processor processor) {
                    processor.updateConfig(config);
                }
                return instance;
            }
        } catch (InvocationTargetException e) {
            throw rethrow(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Could not instantiate " + clazz.getName(), e);
        }
    }

    private static Object instantiateSyntaxProcessor(Class<?> clazz, String name, Map<String, Object> config) {
        try {
            try {
                return clazz.getConstructor(String.class, Map.class).newInstance(name,
                        config == null ? new LinkedHashMap<>() : config);
            } catch (NoSuchMethodException e) {
                try {
                    return clazz.getConstructor(String.class).newInstance(name);
                } catch (NoSuchMethodException e2) {
                    Object instance = clazz.getConstructor().newInstance();
                    if (instance instanceof Processor processor) {
                        processor.updateConfig(config);
                    }
                    return instance;
                }
            }
        } catch (InvocationTargetException e) {
            throw rethrow(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Could not instantiate " + clazz.getName(), e);
        }
    }

    private static RuntimeException rethrow(Throwable cause) {
        return cause instanceof RuntimeException runtimeException ? runtimeException : new IllegalStateException(cause);
    }

    private void reset() {
        preprocessorExtensions = null;
        treeProcessorExtensions = null;
        postprocessorExtensions = null;
        includeProcessorExtensions = null;
        docinfoProcessorExtensions = null;
        blockExtensions = null;
        blockMacroExtensions = null;
        inlineMacroExtensions = null;
        document = null;
    }

    @Override
    public String toString() {
        return "#<" + getClass().getName() + "@" + System.identityHashCode(this) + " {groups: "
                + Strings.inspect(new ArrayList<>(groups.keySet())) + "}>";
    }
}
