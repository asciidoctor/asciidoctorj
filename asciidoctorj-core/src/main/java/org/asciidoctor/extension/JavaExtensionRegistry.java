package org.asciidoctor.extension;

import org.asciidoctor.extension.processorproxies.BlockMacroProcessorProxy;
import org.asciidoctor.extension.processorproxies.BlockProcessorProxy;
import org.asciidoctor.extension.processorproxies.DocinfoProcessorProxy;
import org.asciidoctor.extension.processorproxies.IncludeProcessorProxy;
import org.asciidoctor.extension.processorproxies.InlineMacroProcessorProxy;
import org.asciidoctor.extension.processorproxies.PostprocessorProxy;
import org.asciidoctor.extension.processorproxies.PreprocessorProxy;
import org.asciidoctor.extension.processorproxies.TreeprocessorProxy;
import org.asciidoctor.internal.AsciidoctorModule;
import org.jruby.Ruby;
import org.jruby.RubyClass;

public class JavaExtensionRegistry {

    private AsciidoctorModule asciidoctorModule;
    private Ruby rubyRuntime;
    
    public JavaExtensionRegistry(AsciidoctorModule asciidoctorModule, Ruby rubyRuntime) {
        super();
        this.asciidoctorModule = asciidoctorModule;
        this.rubyRuntime = rubyRuntime;
    }

    public ExtensionRegistration docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor) {
        RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.docinfo_processor(registration.getName(rubyRuntime), rubyClass);
        return registration;
    }

    public ExtensionRegistration docinfoProcessor(DocinfoProcessor docInfoProcessor) {
        RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        asciidoctorModule.docinfo_processor(registration.getName(rubyRuntime), rubyClass);
        return registration;
    }

    public ExtensionRegistration docinfoProcessor(String docInfoProcessor) {
        try {
            Class<? extends DocinfoProcessor>  docinfoProcessorClass = (Class<? extends DocinfoProcessor>) Class.forName(docInfoProcessor);
            return docinfoProcessor(docinfoProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ExtensionRegistration preprocessor(Class<? extends Preprocessor> preprocessor) {
        RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.preprocessor(registration.getName(rubyRuntime), rubyClass);
        return registration;
    }

    public ExtensionRegistration preprocessor(Preprocessor preprocessor) {
        RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.preprocessor(registration.getName(rubyRuntime), rubyClass);
        return registration;
    }
    
    public ExtensionRegistration preprocessor(String preprocessor) {
        try {
            Class<? extends Preprocessor>  preprocessorClass = (Class<? extends Preprocessor>) Class.forName(preprocessor);
            return preprocessor(preprocessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ExtensionRegistration postprocessor(String postprocessor) {
        try {
            Class<? extends Postprocessor>  postprocessorClass = (Class<? extends Postprocessor>) Class.forName(postprocessor);
            return postprocessor(postprocessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ExtensionRegistration postprocessor(Class<? extends Postprocessor> postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.postprocessor(registration.getName(rubyRuntime), rubyClass);
        return registration;
    }
    
    public ExtensionRegistration postprocessor(Postprocessor postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.postprocessor(registration.getName(rubyRuntime), rubyClass);
        return registration;
    }

    public ExtensionRegistration includeProcessor(String includeProcessor) {
        try {
            Class<? extends IncludeProcessor>  includeProcessorClass = (Class<? extends IncludeProcessor>) Class.forName(includeProcessor);
            return includeProcessor(includeProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ExtensionRegistration includeProcessor(
            Class<? extends IncludeProcessor> includeProcessor) {
        RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.include_processor(registration.getName(rubyRuntime), rubyClass);
        return registration;
    }

    public ExtensionRegistration includeProcessor(IncludeProcessor includeProcessor) {
        RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.include_processor(registration.getName(rubyRuntime), rubyClass);
        return registration;
    }
    
    public ExtensionRegistration treeprocessor(Treeprocessor treeprocessor) {
        RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, treeprocessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.treeprocessor(registration.getName(rubyRuntime), rubyClass);
        return registration;
    }

    public ExtensionRegistration treeprocessor(Class<? extends Treeprocessor> abstractTreeProcessor) {
        RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, abstractTreeProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.treeprocessor(registration.getName(rubyRuntime), rubyClass);
        return registration;
    }
    
    public ExtensionRegistration treeprocessor(String treeProcessor) {
        try {
            Class<? extends Treeprocessor>  treeProcessorClass = (Class<? extends Treeprocessor>) Class.forName(treeProcessor);
            return treeprocessor(treeProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ExtensionRegistration block(String blockName,
           String blockProcessor) {
        try {
            Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
            return block(blockName, blockProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ExtensionRegistration block(String blockProcessor) {
        try {
            Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
            return block(blockProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ExtensionRegistration block(String blockName,
            Class<? extends BlockProcessor> blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.block_processor(registration.getName(rubyRuntime), rubyClass, blockName);
        return registration;
    }

    public ExtensionRegistration block(Class<? extends BlockProcessor> blockProcessor) {
        String name = getName(blockProcessor);
        return block(name, blockProcessor);
    }

    public ExtensionRegistration block(BlockProcessor blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.block_processor(registration.getName(rubyRuntime), rubyClass, blockProcessor.getName());
        return registration;
    }

    public ExtensionRegistration block(String blockName,
            BlockProcessor blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.block_processor(registration.getName(rubyRuntime), rubyClass, blockName);
        return registration;
    }

    public ExtensionRegistration blockMacro(String blockName,
            Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.block_macro(registration.getName(rubyRuntime), rubyClass, blockName);
        return registration;
    }

    public ExtensionRegistration blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        String name = getName(blockMacroProcessor);
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.block_macro(registration.getName(rubyRuntime), rubyClass, name);
        return registration;
    }

    public ExtensionRegistration blockMacro(String blockName,
            String blockMacroProcessor) {
        try {
            Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
            return blockMacro(blockName, blockMacroProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ExtensionRegistration blockMacro(String blockMacroProcessor) {
        try {
            Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
            return blockMacro(blockMacroProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ExtensionRegistration blockMacro(BlockMacroProcessor blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.block_macro(registration.getName(rubyRuntime), rubyClass, blockMacroProcessor.getName());
        return registration;
    }

    public ExtensionRegistration inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.inline_macro(registration.getName(rubyRuntime), rubyClass, inlineMacroProcessor.getName());
        return registration;
    }
    
    public ExtensionRegistration inlineMacro(String blockName,
            Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.inline_macro(registration.getName(rubyRuntime), rubyClass, blockName);
        return registration;
    }

    public ExtensionRegistration inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        String name = getName(inlineMacroProcessor);
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.inline_macro(registration.getName(rubyRuntime), rubyClass, name);
        return registration;
    }

    public ExtensionRegistration inlineMacro(String blockName, String inlineMacroProcessor) {
        try {
            Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
            return inlineMacro(blockName, inlineMacroProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ExtensionRegistration inlineMacro(String inlineMacroProcessor) {
        try {
            Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
            return inlineMacro(inlineMacroProcessorClass);
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
