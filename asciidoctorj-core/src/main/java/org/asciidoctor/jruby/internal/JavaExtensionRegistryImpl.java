package org.asciidoctor.jruby.internal;

import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.jruby.extension.processorproxies.BlockMacroProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.BlockProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.DocinfoProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.IncludeProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.InlineMacroProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.PostprocessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.PreprocessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.TreeprocessorProxy;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaExtensionRegistryImpl implements JavaExtensionRegistry {

    private final JRubyAsciidoctor asciidoctor;

    private Ruby rubyRuntime;

    public JavaExtensionRegistryImpl(final JRubyAsciidoctor asciidoctor) {
        this.rubyRuntime = asciidoctor.getRubyRuntime();
        this.asciidoctor = asciidoctor;
    }

    @Override
    public JavaExtensionRegistry docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor) {
        RubyClass rubyClass = DocinfoProcessorProxy.register(asciidoctor, docInfoProcessor);
        getAsciidoctorModule().callMethod("docinfo_processor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry docinfoProcessor(DocinfoProcessor docInfoProcessor) {
        RubyClass rubyClass = DocinfoProcessorProxy.register(asciidoctor, docInfoProcessor);
        getAsciidoctorModule().callMethod("docinfo_processor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry docinfoProcessor(String docInfoProcessor) {
        try {
            Class<? extends DocinfoProcessor> docinfoProcessorClass = (Class<? extends DocinfoProcessor>) Class.forName(docInfoProcessor);
            docinfoProcessor(docinfoProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaExtensionRegistry preprocessor(Class<? extends Preprocessor> preprocessor) {
        RubyClass rubyClass = PreprocessorProxy.register(asciidoctor, preprocessor);
        getAsciidoctorModule().callMethod("preprocessor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry preprocessor(Preprocessor preprocessor) {
        RubyClass rubyClass = PreprocessorProxy.register(asciidoctor, preprocessor);
        getAsciidoctorModule().callMethod("preprocessor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry preprocessor(String preprocessor) {
        try {
            Class<? extends Preprocessor> preprocessorClass = (Class<? extends Preprocessor>) Class.forName(preprocessor);
            preprocessor(preprocessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaExtensionRegistry postprocessor(String postprocessor) {
        try {
            Class<? extends Postprocessor> postprocessorClass = (Class<? extends Postprocessor>) Class.forName(postprocessor);
            postprocessor(postprocessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaExtensionRegistry postprocessor(Class<? extends Postprocessor> postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(asciidoctor, postprocessor);
        getAsciidoctorModule().callMethod("postprocessor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry postprocessor(Postprocessor postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(asciidoctor, postprocessor);
        getAsciidoctorModule().callMethod("postprocessor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry includeProcessor(String includeProcessor) {
        try {
            Class<? extends IncludeProcessor> includeProcessorClass = (Class<? extends IncludeProcessor>) Class.forName(includeProcessor);
            includeProcessor(includeProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaExtensionRegistry includeProcessor(
            Class<? extends IncludeProcessor> includeProcessor) {
        RubyClass rubyClass = IncludeProcessorProxy.register(asciidoctor, includeProcessor);
        getAsciidoctorModule().callMethod("include_processor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry includeProcessor(IncludeProcessor includeProcessor) {
        RubyClass rubyClass = IncludeProcessorProxy.register(asciidoctor, includeProcessor);
        getAsciidoctorModule().callMethod("include_processor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry treeprocessor(Treeprocessor treeprocessor) {
        RubyClass rubyClass = TreeprocessorProxy.register(asciidoctor, treeprocessor);
        getAsciidoctorModule().callMethod("treeprocessor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry treeprocessor(Class<? extends Treeprocessor> abstractTreeProcessor) {
        RubyClass rubyClass = TreeprocessorProxy.register(asciidoctor, abstractTreeProcessor);
        getAsciidoctorModule().callMethod("treeprocessor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry treeprocessor(String treeProcessor) {
        try {
            Class<? extends Treeprocessor> treeProcessorClass = (Class<? extends Treeprocessor>) Class.forName(treeProcessor);
            treeprocessor(treeProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaExtensionRegistry block(String blockName,
                                       String blockProcessor) {
        try {
            Class<? extends BlockProcessor> blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
            block(blockName, blockProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaExtensionRegistry block(String blockProcessor) {
        try {
            Class<? extends BlockProcessor> blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
            block(blockProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaExtensionRegistry block(String blockName,
                                       Class<? extends BlockProcessor> blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(asciidoctor, blockProcessor);
        getAsciidoctorModule().callMethod("block_processor", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    @Override
    public JavaExtensionRegistry block(Class<? extends BlockProcessor> blockProcessor) {
        getNames(blockProcessor).forEach(name -> {
            block(name, blockProcessor);
        });
        return this;
    }

    @Override
    public JavaExtensionRegistry block(BlockProcessor blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(asciidoctor, blockProcessor);
        getAsciidoctorModule().callMethod("block_processor", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry block(String blockName,
                                       BlockProcessor blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(asciidoctor, blockProcessor);
        getAsciidoctorModule().callMethod("block_processor", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String blockName,
                                            Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(asciidoctor, blockMacroProcessor);
        getAsciidoctorModule().callMethod("block_macro", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        getNames(blockMacroProcessor).forEach(name -> {
            RubyClass rubyClass = BlockMacroProcessorProxy.register(asciidoctor, blockMacroProcessor);
            getAsciidoctorModule().callMethod("block_macro", rubyClass, rubyRuntime.newString(name));
        });
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String blockName,
                                            String blockMacroProcessor) {
        try {
            Class<? extends BlockMacroProcessor> blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
            blockMacro(blockName, blockMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaExtensionRegistry blockMacro(String blockMacroProcessor) {
        try {
            Class<? extends BlockMacroProcessor> blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
            blockMacro(blockMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaExtensionRegistry blockMacro(BlockMacroProcessor blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(asciidoctor, blockMacroProcessor);
        getAsciidoctorModule().callMethod("block_macro", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String macroName, BlockMacroProcessor blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(asciidoctor, blockMacroProcessor);
        getAsciidoctorModule().callMethod("block_macro", rubyClass, rubyRuntime.newString(macroName));
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String macroName,
                                             InlineMacroProcessor inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(asciidoctor, inlineMacroProcessor);
        getAsciidoctorModule().callMethod("inline_macro", rubyClass, rubyRuntime.newString(macroName));
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(asciidoctor, inlineMacroProcessor);
        getAsciidoctorModule().callMethod("inline_macro", rubyClass);
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String macroName,
                                             Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(asciidoctor, inlineMacroProcessor);
        getAsciidoctorModule().callMethod("inline_macro", rubyClass, rubyRuntime.newString(macroName));
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        getNames(inlineMacroProcessor).forEach(name -> {
            RubyClass rubyClass = InlineMacroProcessorProxy.register(asciidoctor, inlineMacroProcessor);
            getAsciidoctorModule().callMethod("inline_macro", rubyClass, rubyRuntime.newString(name));
        });
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String macroName, String inlineMacroProcessor) {
        try {
            Class<? extends InlineMacroProcessor> inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
            inlineMacro(macroName, inlineMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String inlineMacroProcessor) {
        try {
            Class<? extends InlineMacroProcessor> inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
            inlineMacro(inlineMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Name> getNameAnnotations(Annotation[] annotations, Set<Annotation> visitedAnnotations) {
        Set<Name> allNameAnnotations = new HashSet<>();
        for (Annotation anno : annotations) {
            if (visitedAnnotations.contains(anno)) {
                continue;
            }
            if (anno.annotationType().isAssignableFrom(Name.class)) {
                allNameAnnotations.add((Name) anno);
            } else {
                visitedAnnotations.add(anno);
                allNameAnnotations.addAll(getNameAnnotations(anno.annotationType().getAnnotations(), visitedAnnotations));
            }
        }
        return allNameAnnotations;
    }

    private Set<String> getNames(Class<?> clazz) {
        Set<String> nameAnnotations = getNameAnnotations(clazz.getAnnotations(), new HashSet<Annotation>()).stream().map(Name::value).distinct().collect(Collectors.toSet());
        if (nameAnnotations.isEmpty()) {
            throw new IllegalArgumentException(clazz + " must be registered with a name or it must have a Name annotation!");
        } else {
            return nameAnnotations;
        }
    }

    private RubyModule getAsciidoctorModule() {
        return rubyRuntime.getModule("AsciidoctorModule");
    }
}
