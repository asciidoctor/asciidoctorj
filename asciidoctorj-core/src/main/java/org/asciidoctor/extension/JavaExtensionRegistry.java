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
        RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
        this.asciidoctorModule.docinfo_processor(rubyClass);
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
        RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
        this.asciidoctorModule.preprocessor(rubyClass);
    }
    
    public void postprocessor(String postprocessor) {
        RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
        this.asciidoctorModule.postprocessor(rubyClass);
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
        RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
        this.asciidoctorModule.include_processor(rubyClass);
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
        RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, treeProcessor);
        this.asciidoctorModule.treeprocessor(rubyClass);
    }

    public void block(String blockName,
           String blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        this.asciidoctorModule.block_processor(rubyClass, blockName);
    }

    public void block(String blockName,
            Class<? extends BlockProcessor> blockProcessor) {
        RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
        this.asciidoctorModule.block_processor(rubyClass, blockName);
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

    public void blockMacro(String blockName,
            String blockMacroProcessor) {
        RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
        this.asciidoctorModule.block_macro(rubyClass, blockName);
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
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor, blockName);
        this.asciidoctorModule.inline_macro(rubyClass, blockName);
    }
    
    public void inlineMacro(String blockName, String inlineMacroProcessor) {
        RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor, blockName);
        this.asciidoctorModule.inline_macro(rubyClass, blockName);
    }
}
