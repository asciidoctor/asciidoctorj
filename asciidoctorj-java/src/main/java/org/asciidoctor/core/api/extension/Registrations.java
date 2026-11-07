package org.asciidoctor.core.api.extension;

import static org.asciidoctor.core.api.extension.ProcessorProxies.instantiate;
import static org.asciidoctor.core.api.extension.ProcessorProxies.loadClass;
import static org.asciidoctor.core.api.extension.ProcessorProxies.newConfig;
import static org.asciidoctor.core.api.extension.ProcessorProxies.resolveName;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.asciidoctor.core.api.extension.ProcessorProxies.BlockMacroProcessorProxy;
import org.asciidoctor.core.api.extension.ProcessorProxies.BlockProcessorProxy;
import org.asciidoctor.core.api.extension.ProcessorProxies.DocinfoProcessorProxy;
import org.asciidoctor.core.api.extension.ProcessorProxies.IncludeProcessorProxy;
import org.asciidoctor.core.api.extension.ProcessorProxies.InlineMacroProcessorProxy;
import org.asciidoctor.core.api.extension.ProcessorProxies.PostprocessorProxy;
import org.asciidoctor.core.api.extension.ProcessorProxies.PreprocessorProxy;
import org.asciidoctor.core.api.extension.ProcessorProxies.TreeprocessorProxy;
import org.asciidoctor.core.api.CoreAsciidoctor;
import org.asciidoctor.core.extension.Registry;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.MacroProcessor;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Processor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Treeprocessor;

/**
 * Creates the registrations of the processors of the AsciidoctorJ API, which are applied to the extension registry
 * of the implementation when it's activated (i.e., for each document).
 *
 * <p>A processor instance is registered as is (and its configuration is finalized immediately), whereas a processor
 * class is instantiated each time the registry is activated (like a Ruby extension class).
 */
final class Registrations {
    private final CoreAsciidoctor asciidoctor;

    Registrations(CoreAsciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    /**
     * Finalizes the configuration of the processor and associates it with the Asciidoctor instance.
     */
    private void finalizeConfig(Processor processor) {
        ProcessorProxies.finalizeConfig(processor);
        processor.unwrap(ProcessorImpl.class).setAsciidoctor(asciidoctor);
    }

    /**
     * @return the names declared by the Name annotations of the processor class, including the meta-annotations (e.g.,
     * an annotation annotated with Name)
     * @throws IllegalArgumentException if the class has no Name annotation
     */
    static Set<String> names(Class<?> type) {
        Set<String> names = new LinkedHashSet<>();
        collectNames(type.getAnnotations(), new HashSet<>(), names);
        if (names.isEmpty()) {
            throw new IllegalArgumentException(type + " must be registered with a name or it must have a Name annotation!");
        }
        return names;
    }

    private static void collectNames(Annotation[] annotations, Set<Annotation> visited, Set<String> names) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Name name) {
                names.add(name.value());
            } else if (visited.add(annotation)) {
                collectNames(annotation.annotationType().getAnnotations(), visited, names);
            }
        }
    }

    // --- preprocessor ---

    Consumer<Registry> preprocessor(Preprocessor processor) {
        finalizeConfig(processor);
        return registry -> registry.preprocessor(new PreprocessorProxy(processor));
    }

    Consumer<Registry> preprocessor(Class<? extends Preprocessor> type) {
        return registry -> {
            Preprocessor processor = instantiate(type, newConfig());
            finalizeConfig(processor);
            registry.preprocessor(new PreprocessorProxy(processor));
        };
    }

    Consumer<Registry> preprocessor(String className) {
        return preprocessor(loadClass(className, Preprocessor.class));
    }

    // --- tree processor ---

    Consumer<Registry> treeprocessor(Treeprocessor processor) {
        finalizeConfig(processor);
        return registry -> registry.treeProcessor(new TreeprocessorProxy(processor));
    }

    Consumer<Registry> treeprocessor(Class<? extends Treeprocessor> type) {
        return registry -> {
            Treeprocessor processor = instantiate(type, newConfig());
            finalizeConfig(processor);
            registry.treeProcessor(new TreeprocessorProxy(processor));
        };
    }

    Consumer<Registry> treeprocessor(String className) {
        return treeprocessor(loadClass(className, Treeprocessor.class));
    }

    // --- postprocessor ---

    Consumer<Registry> postprocessor(Postprocessor processor) {
        finalizeConfig(processor);
        return registry -> registry.postprocessor(new PostprocessorProxy(processor));
    }

    Consumer<Registry> postprocessor(Class<? extends Postprocessor> type) {
        return registry -> {
            Postprocessor processor = instantiate(type, newConfig());
            finalizeConfig(processor);
            registry.postprocessor(new PostprocessorProxy(processor));
        };
    }

    Consumer<Registry> postprocessor(String className) {
        return postprocessor(loadClass(className, Postprocessor.class));
    }

    // --- include processor ---

    Consumer<Registry> includeProcessor(IncludeProcessor processor) {
        finalizeConfig(processor);
        return registry -> registry.includeProcessor(new IncludeProcessorProxy(processor));
    }

    Consumer<Registry> includeProcessor(Class<? extends IncludeProcessor> type) {
        return registry -> {
            IncludeProcessor processor = instantiate(type, newConfig());
            finalizeConfig(processor);
            registry.includeProcessor(new IncludeProcessorProxy(processor));
        };
    }

    Consumer<Registry> includeProcessor(String className) {
        return includeProcessor(loadClass(className, IncludeProcessor.class));
    }

    // --- docinfo processor ---

    Consumer<Registry> docinfoProcessor(DocinfoProcessor processor) {
        finalizeConfig(processor);
        return registry -> registry.docinfoProcessor(new DocinfoProcessorProxy(processor));
    }

    Consumer<Registry> docinfoProcessor(Class<? extends DocinfoProcessor> type) {
        return registry -> {
            DocinfoProcessor processor = instantiate(type, newConfig());
            finalizeConfig(processor);
            registry.docinfoProcessor(new DocinfoProcessorProxy(processor));
        };
    }

    Consumer<Registry> docinfoProcessor(String className) {
        return docinfoProcessor(loadClass(className, DocinfoProcessor.class));
    }

    // --- block processor ---

    Consumer<Registry> block(String name, BlockProcessor processor) {
        finalizeConfig(processor);
        return registry -> registerBlock(registry, name, processor);
    }

    Consumer<Registry> block(String name, Class<? extends BlockProcessor> type) {
        return registry -> {
            BlockProcessor processor = instantiate(type, name, newConfig());
            finalizeConfig(processor);
            registerBlock(registry, name, processor);
        };
    }

    Consumer<Registry> block(String name, String className) {
        return block(name, loadClass(className, BlockProcessor.class));
    }

    private void registerBlock(Registry registry, String explicitName, BlockProcessor processor) {
        String name = resolveName(explicitName, processor.getName(), processor.getConfig());
        processor.setName(name);
        registry.block(new BlockProcessorProxy(processor, name), name);
    }

    // --- block macro processor ---

    Consumer<Registry> blockMacro(String name, BlockMacroProcessor processor) {
        finalizeConfig(processor);
        return registry -> registerBlockMacro(registry, name, processor);
    }

    Consumer<Registry> blockMacro(String name, Class<? extends BlockMacroProcessor> type) {
        return registry -> {
            BlockMacroProcessor processor = instantiate(type, name, newConfig());
            finalizeConfig(processor);
            registerBlockMacro(registry, name, processor);
        };
    }

    Consumer<Registry> blockMacro(String name, String className) {
        return blockMacro(name, loadClass(className, BlockMacroProcessor.class));
    }

    private void registerBlockMacro(Registry registry, String explicitName, BlockMacroProcessor processor) {
        String name = resolveMacroName(explicitName, processor);
        registry.blockMacro(new BlockMacroProcessorProxy(processor, name), name);
    }

    // --- inline macro processor ---

    Consumer<Registry> inlineMacro(String name, InlineMacroProcessor processor) {
        finalizeConfig(processor);
        return registry -> registerInlineMacro(registry, name, processor);
    }

    Consumer<Registry> inlineMacro(String name, Class<? extends InlineMacroProcessor> type) {
        return registry -> {
            InlineMacroProcessor processor = instantiate(type, name, newConfig());
            finalizeConfig(processor);
            registerInlineMacro(registry, name, processor);
        };
    }

    Consumer<Registry> inlineMacro(String name, String className) {
        return inlineMacro(name, loadClass(className, InlineMacroProcessor.class));
    }

    private void registerInlineMacro(Registry registry, String explicitName, InlineMacroProcessor processor) {
        String name = resolveMacroName(explicitName, processor);
        registry.inlineMacro(new InlineMacroProcessorProxy(processor, name), name);
    }

    private static String resolveMacroName(String explicitName, MacroProcessor<?> processor) {
        String name = resolveName(explicitName, processor.getName(), processor.getConfig());
        processor.setName(name);
        return name;
    }
}
