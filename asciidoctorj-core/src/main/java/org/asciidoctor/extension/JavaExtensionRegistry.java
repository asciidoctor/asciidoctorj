package org.asciidoctor.extension;

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
import org.jruby.internal.runtime.methods.DynamicMethod;

import java.util.Map;

public class JavaExtensionRegistry {

    private Ruby rubyRuntime;
    
    public JavaExtensionRegistry(final Ruby rubyRuntime) {
        this.rubyRuntime = rubyRuntime;
    }

    public JavaExtensionRegistry docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor) {

        RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "docinfo_processor", rubyClass);
        return this;
    }

    public JavaExtensionRegistry docinfoProcessor(DocinfoProcessor docInfoProcessor) {
        RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "docinfo_processor", rubyClass);
        return this;
    }

    public JavaExtensionRegistry docinfoProcessor(String docInfoProcessor) {
        try {
            Class<? extends DocinfoProcessor>  docinfoProcessorClass = (Class<? extends DocinfoProcessor>) Class.forName(docInfoProcessor);
            docinfoProcessor(docinfoProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistry preprocessor(Class<? extends Preprocessor> preprocessor) {
        RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "preprocessor", rubyClass);
        return this;
    }

    public JavaExtensionRegistry preprocessor(Preprocessor preprocessor) {
        RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);

        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "preprocessor", rubyClass);
        return this;
    }
    
    public JavaExtensionRegistry preprocessor(String preprocessor) {
        try {
            Class<? extends Preprocessor>  preprocessorClass = (Class<? extends Preprocessor>) Class.forName(preprocessor);
            preprocessor(preprocessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public JavaExtensionRegistry postprocessor(String postprocessor) {
        try {
            Class<? extends Postprocessor>  postprocessorClass = (Class<? extends Postprocessor>) Class.forName(postprocessor);
            postprocessor(postprocessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public JavaExtensionRegistry postprocessor(Class<? extends Postprocessor> postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "postprocessor", rubyClass);
        return this;
    }
    
    public JavaExtensionRegistry postprocessor(Postprocessor postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "postprocessor", rubyClass);
        return this;
    }

    public JavaExtensionRegistry includeProcessor(String includeProcessor) {
        try {
            Class<? extends IncludeProcessor>  includeProcessorClass = (Class<? extends IncludeProcessor>) Class.forName(includeProcessor);
            includeProcessor(includeProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public JavaExtensionRegistry includeProcessor(
            Class<? extends IncludeProcessor> includeProcessor) {
        RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "include_processor", rubyClass);
        return this;
    }

    public JavaExtensionRegistry includeProcessor(IncludeProcessor includeProcessor) {
        RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "include_processor", rubyClass);
        return this;
    }
    
    public JavaExtensionRegistry treeprocessor(Treeprocessor treeprocessor) {
        RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, treeprocessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "treeprocessor", rubyClass);
        return this;
    }

    public JavaExtensionRegistry treeprocessor(Class<? extends Treeprocessor> abstractTreeProcessor) {
        RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, abstractTreeProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "treeprocessor", rubyClass);
        return this;
    }
    
    public JavaExtensionRegistry treeprocessor(String treeProcessor) {
        try {
            Class<? extends Treeprocessor>  treeProcessorClass = (Class<? extends Treeprocessor>) Class.forName(treeProcessor);
            treeprocessor(treeProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistry block(String blockName,
           String blockProcessor) {
        try {
            Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
            block(blockName, blockProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistry block(String blockProcessor) {
        try {
            Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
            block(blockProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistry block(String blockName,
            Class<? extends BlockProcessor> blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "block_processor", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    public JavaExtensionRegistry block(Class<? extends BlockProcessor> blockProcessor) {
        String name = getName(blockProcessor);
        block(name, blockProcessor);
        return this;
    }

    public JavaExtensionRegistry block(BlockProcessor blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "block_processor", rubyClass, rubyRuntime.newString(blockProcessor.getName()));
        return this;
    }

    public JavaExtensionRegistry block(String blockName,
            BlockProcessor blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "block_processor", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    public JavaExtensionRegistry blockMacro(String blockName,
            Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "block_macro", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    public JavaExtensionRegistry blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        String name = getName(blockMacroProcessor);
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "block_macro", rubyClass, rubyRuntime.newString(name));
        return this;
    }

    public JavaExtensionRegistry blockMacro(String blockName,
            String blockMacroProcessor) {
        try {
            Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
            blockMacro(blockName, blockMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistry blockMacro(String blockMacroProcessor) {
        try {
            Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
            blockMacro(blockMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistry blockMacro(BlockMacroProcessor blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "block_macro", rubyClass, rubyRuntime.newString(blockMacroProcessor.getName()));
        return this;
    }

    public JavaExtensionRegistry inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "inline_macro", rubyClass, rubyRuntime.newString(inlineMacroProcessor.getName()));
        return this;
    }
    
    public JavaExtensionRegistry inlineMacro(String blockName,
            Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "inline_macro", rubyClass, rubyRuntime.newString(blockName));
        return this;
    }

    public JavaExtensionRegistry inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        String name = getName(inlineMacroProcessor);
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "inline_macro", rubyClass, rubyRuntime.newString(name));
        return this;
    }

    public JavaExtensionRegistry inlineMacro(String blockName, String inlineMacroProcessor) {
        try {
            Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
            inlineMacro(blockName, inlineMacroProcessorClass);
            return this;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaExtensionRegistry inlineMacro(String inlineMacroProcessor) {
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
}
