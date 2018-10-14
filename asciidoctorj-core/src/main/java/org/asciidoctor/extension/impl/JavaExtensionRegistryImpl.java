package org.asciidoctor.extension.impl;

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
import org.asciidoctor.extension.processorproxies.BlockMacroProcessorProxy;
import org.asciidoctor.extension.processorproxies.BlockProcessorProxy;
import org.asciidoctor.extension.processorproxies.DocinfoProcessorProxy;
import org.asciidoctor.extension.processorproxies.IncludeProcessorProxy;
import org.asciidoctor.extension.processorproxies.InlineMacroProcessorProxy;
import org.asciidoctor.extension.processorproxies.PostprocessorProxy;
import org.asciidoctor.extension.processorproxies.PreprocessorProxy;
import org.asciidoctor.extension.processorproxies.TreeprocessorProxy;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;

public class JavaExtensionRegistryImpl implements JavaExtensionRegistry {

    private Ruby rubyRuntime;

    public JavaExtensionRegistryImpl(final Ruby rubyRuntime) {
        this.rubyRuntime = rubyRuntime;
    }

    public JavaExtensionRegistryImpl docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor) {

        RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
        getAsciidoctorModule()
            .callMethod( "docinfo_processor", rubyClass);
        return this;
    }

    public JavaExtensionRegistryImpl docinfoProcessor(DocinfoProcessor docInfoProcessor) {
        RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
        getAsciidoctorModule()
            .callMethod( "docinfo_processor", rubyClass);
        return this;
    }

    public JavaExtensionRegistryImpl docinfoProcessor(String docInfoProcessor) {
        try {
            Class<? extends DocinfoProcessor>  docinfoProcessorClass = (Class<? extends DocinfoProcessor>) Class.forName(docInfoProcessor);
            docinfoProcessor(docinfoProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistryImpl preprocessor(Class<? extends Preprocessor> preprocessor) {
        RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
        getAsciidoctorModule()
            .callMethod( "preprocessor", rubyClass);
        return this;
    }

    public JavaExtensionRegistryImpl preprocessor(Preprocessor preprocessor) {
        RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);

        getAsciidoctorModule()
            .callMethod( "preprocessor", rubyClass);
        return this;
    }
    
    public JavaExtensionRegistryImpl preprocessor(String preprocessor) {
        try {
            Class<? extends Preprocessor>  preprocessorClass = (Class<? extends Preprocessor>) Class.forName(preprocessor);
            preprocessor(preprocessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public JavaExtensionRegistryImpl postprocessor(String postprocessor) {
        try {
            Class<? extends Postprocessor>  postprocessorClass = (Class<? extends Postprocessor>) Class.forName(postprocessor);
            postprocessor(postprocessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public JavaExtensionRegistryImpl postprocessor(Class<? extends Postprocessor> postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
        getAsciidoctorModule()
            .callMethod( "postprocessor", rubyClass);
        return this;
    }
    
    public JavaExtensionRegistryImpl postprocessor(Postprocessor postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
        getAsciidoctorModule()
            .callMethod( "postprocessor", rubyClass);
        return this;
    }

    public JavaExtensionRegistryImpl includeProcessor(String includeProcessor) {
        try {
            Class<? extends IncludeProcessor>  includeProcessorClass = (Class<? extends IncludeProcessor>) Class.forName(includeProcessor);
            includeProcessor(includeProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public JavaExtensionRegistryImpl includeProcessor(
            Class<? extends IncludeProcessor> includeProcessor) {
        RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
        getAsciidoctorModule()
            .callMethod( "include_processor", rubyClass);
        return this;
    }

    public JavaExtensionRegistryImpl includeProcessor(IncludeProcessor includeProcessor) {
        RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
        getAsciidoctorModule()
            .callMethod( "include_processor", rubyClass);
        return this;
    }
    
    public JavaExtensionRegistryImpl treeprocessor(Treeprocessor treeprocessor) {
        RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, treeprocessor);
        getAsciidoctorModule()
            .callMethod( "treeprocessor", rubyClass);
        return this;
    }

    public JavaExtensionRegistryImpl treeprocessor(Class<? extends Treeprocessor> abstractTreeProcessor) {
        RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, abstractTreeProcessor);
        getAsciidoctorModule()
            .callMethod( "treeprocessor", rubyClass);
        return this;
    }
    
    public JavaExtensionRegistryImpl treeprocessor(String treeProcessor) {
        try {
            Class<? extends Treeprocessor>  treeProcessorClass = (Class<? extends Treeprocessor>) Class.forName(treeProcessor);
            treeprocessor(treeProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistryImpl block(String blockName,
                                           String blockProcessor) {
        try {
            Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
            block(blockName, blockProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistryImpl block(String blockProcessor) {
        try {
            Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
            block(blockProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistryImpl block(String blockName,
                                           Class<? extends BlockProcessor> blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        getAsciidoctorModule()
            .callMethod( "block_processor", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    public JavaExtensionRegistryImpl block(Class<? extends BlockProcessor> blockProcessor) {
        String name = getName(blockProcessor);
        block(name, blockProcessor);
        return this;
    }

    public JavaExtensionRegistryImpl block(BlockProcessor blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        getAsciidoctorModule()
            .callMethod( "block_processor", rubyClass, rubyRuntime.newString(blockProcessor.getName()));
        return this;
    }

    public JavaExtensionRegistryImpl block(String blockName,
                                           BlockProcessor blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        getAsciidoctorModule()
            .callMethod( "block_processor", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    public JavaExtensionRegistryImpl blockMacro(String blockName,
                                                Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        getAsciidoctorModule()
            .callMethod( "block_macro", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    public JavaExtensionRegistryImpl blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        String name = getName(blockMacroProcessor);
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        getAsciidoctorModule()
            .callMethod( "block_macro", rubyClass, rubyRuntime.newString(name));
        return this;
    }

    public JavaExtensionRegistryImpl blockMacro(String blockName,
                                                String blockMacroProcessor) {
        try {
            Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
            blockMacro(blockName, blockMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistryImpl blockMacro(String blockMacroProcessor) {
        try {
            Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
            blockMacro(blockMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistryImpl blockMacro(BlockMacroProcessor blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        getAsciidoctorModule()
            .callMethod( "block_macro", rubyClass, rubyRuntime.newString(blockMacroProcessor.getName()));
        return this;
    }

    public JavaExtensionRegistryImpl inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        getAsciidoctorModule()
            .callMethod( "inline_macro", rubyClass, rubyRuntime.newString(inlineMacroProcessor.getName()));
        return this;
    }
    
    public JavaExtensionRegistryImpl inlineMacro(String blockName,
                                                 Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        getAsciidoctorModule()
            .callMethod( "inline_macro", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    public JavaExtensionRegistryImpl inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        String name = getName(inlineMacroProcessor);
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        getAsciidoctorModule()
            .callMethod( "inline_macro", rubyClass, rubyRuntime.newString(name));
        return this;
    }

    public JavaExtensionRegistryImpl inlineMacro(String blockName, String inlineMacroProcessor) {
        try {
            Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
            inlineMacro(blockName, inlineMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistryImpl inlineMacro(String inlineMacroProcessor) {
        try {
            Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
            inlineMacro(inlineMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String getName(Class<?> clazz) {
        Name nameAnnotation = clazz.getAnnotation(Name.class);
        if (nameAnnotation == null || nameAnnotation.value() == null) {
            throw new IllegalArgumentException(clazz + " must be registered with a name or it must have a Name annotation!");
        }
        return nameAnnotation.value();
    }

    private RubyModule getAsciidoctorModule() {
        return rubyRuntime.getModule("AsciidoctorModule");
    }
}
