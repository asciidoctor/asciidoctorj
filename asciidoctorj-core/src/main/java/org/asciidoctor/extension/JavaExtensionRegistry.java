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

    public void docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor) {
        RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
        this.asciidoctorModule.docinfo_processor(rubyClass);
    }

    public void docinfoProcessor(DocinfoProcessor docInfoProcessor) {
        RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
        this.asciidoctorModule.docinfo_processor(rubyClass);
    }

    public void docinfoProcessor(String docInfoProcessor) {
        try {
            Class<? extends DocinfoProcessor>  docinfoProcessorClass = (Class<? extends DocinfoProcessor>) Class.forName(docInfoProcessor);
            docinfoProcessor(docinfoProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void preprocessor(Class<? extends Preprocessor> preprocessor) {
        RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
        this.asciidoctorModule.preprocessor(rubyClass);
    }

    public void preprocessor(Preprocessor preprocessor) {
        RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
        this.asciidoctorModule.preprocessor(rubyClass);
    }
    
    public void preprocessor(String preprocessor) {
        try {
            Class<? extends Preprocessor>  preprocessorClass = (Class<? extends Preprocessor>) Class.forName(preprocessor);
            preprocessor(preprocessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void postprocessor(String postprocessor) {
        try {
            Class<? extends Postprocessor>  postprocessorClass = (Class<? extends Postprocessor>) Class.forName(postprocessor);
            postprocessor(postprocessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void postprocessor(Class<? extends Postprocessor> postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
        this.asciidoctorModule.postprocessor(rubyClass);
    }
    
    public void postprocessor(Postprocessor postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
        this.asciidoctorModule.postprocessor(rubyClass);
    }

    public void includeProcessor(String includeProcessor) {
        try {
            Class<? extends IncludeProcessor>  includeProcessorClass = (Class<? extends IncludeProcessor>) Class.forName(includeProcessor);
            includeProcessor(includeProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void includeProcessor(
            Class<? extends IncludeProcessor> includeProcessor) {
        RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
        this.asciidoctorModule.include_processor(rubyClass);
    }

    public void includeProcessor(IncludeProcessor includeProcessor) {
        RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
        this.asciidoctorModule.include_processor(rubyClass);
    }
    
    public void treeprocessor(Treeprocessor treeprocessor) {
        RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, treeprocessor);
        this.asciidoctorModule.treeprocessor(rubyClass);
    }

    public void treeprocessor(Class<? extends Treeprocessor> abstractTreeProcessor) {
        RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, abstractTreeProcessor);
        this.asciidoctorModule.treeprocessor(rubyClass);
    }
    
    public void treeprocessor(String treeProcessor) {
        try {
            Class<? extends Treeprocessor>  treeProcessorClass = (Class<? extends Treeprocessor>) Class.forName(treeProcessor);
            treeprocessor(treeProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void block(String blockName,
           String blockProcessor) {
        try {
            Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
            block(blockName, blockProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void block(String blockProcessor) {
        try {
            Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
            block(blockProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void block(String blockName,
            Class<? extends BlockProcessor> blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        this.asciidoctorModule.block_processor(rubyClass, blockName);
    }

    public void block(Class<? extends BlockProcessor> blockProcessor) {
        String name = getName(blockProcessor);
        block(name, blockProcessor);
    }

    public void block(BlockProcessor blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        this.asciidoctorModule.block_processor(rubyClass, blockProcessor.getName());
    }

    public void block(String blockName,
            BlockProcessor blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        this.asciidoctorModule.block_processor(rubyClass, blockName);
    }

    public void blockMacro(String blockName,
            Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        this.asciidoctorModule.block_macro(rubyClass, blockName);
    }

    public void blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        String name = getName(blockMacroProcessor);
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        this.asciidoctorModule.block_macro(rubyClass, name);
    }

    public void blockMacro(String blockName,
            String blockMacroProcessor) {
        try {
            Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
            blockMacro(blockName, blockMacroProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void blockMacro(String blockMacroProcessor) {
        try {
            Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
            blockMacro(blockMacroProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void blockMacro(BlockMacroProcessor blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        this.asciidoctorModule.block_macro(rubyClass, blockMacroProcessor.getName());
    }

    public void inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        this.asciidoctorModule.inline_macro(rubyClass, inlineMacroProcessor.getName());
    }
    
    public void inlineMacro(String blockName,
            Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        this.asciidoctorModule.inline_macro(rubyClass, blockName);
    }

    public void inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        String name = getName(inlineMacroProcessor);
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
        this.asciidoctorModule.inline_macro(rubyClass, name);
    }

    public void inlineMacro(String blockName, String inlineMacroProcessor) {
        try {
            Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
            inlineMacro(blockName, inlineMacroProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void inlineMacro(String inlineMacroProcessor) {
        try {
            Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
            inlineMacro(inlineMacroProcessorClass);
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
