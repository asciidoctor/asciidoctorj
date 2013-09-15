package org.asciidoctor.extension;

import org.asciidoctor.internal.AsciidoctorModule;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;

public class ExtensionRegistry {

    private AsciidoctorModule asciidoctorModule;
    private Ruby rubyRuntime;
    
    public ExtensionRegistry(AsciidoctorModule asciidoctorModule, Ruby rubyRuntime) {
        super();
        this.asciidoctorModule = asciidoctorModule;
        this.rubyRuntime = rubyRuntime;
    }
    
    public void preprocessor(Class<? extends Preprocessor> preprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + preprocessor.getName());
        this.asciidoctorModule.preprocessor(preprocessor.getSimpleName());
    }

    public void postprocessor(Class<? extends Postprocessor> postprocesor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + postprocesor.getName());
        this.asciidoctorModule.postprocessor(postprocesor.getSimpleName());
    }

    public void includeProcessor(
            Class<? extends IncludeProcessor> includeProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + includeProcessor.getName());
        this.asciidoctorModule.include_processor(includeProcessor
                .getSimpleName());
    }

    public void treeprocessor(Class<? extends Treeprocessor> treeProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime
                .evalScriptlet("java_import " + treeProcessor.getName());
        this.asciidoctorModule.treeprocessor(treeProcessor.getSimpleName());
    }

    public void block(String blockName,
            Class<? extends BlockProcessor> blockProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + blockProcessor.getName());
        try {
            // invoke setup(Ruby) method to convert config Map to RubyHash with symbols
            blockProcessor.getMethod("setup", Ruby.class).invoke(null, this.rubyRuntime);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke setup method on block processor class: " + blockProcessor, e);
        }
        
        this.asciidoctorModule.block_processor(
                RubyUtils.toSymbol(rubyRuntime, blockName),
                blockProcessor.getSimpleName());
    }

    public void blockMacro(String blockName,
            Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + blockMacroProcessor.getName());
        this.asciidoctorModule.block_macro(
                RubyUtils.toSymbol(rubyRuntime, blockName),
                blockMacroProcessor.getSimpleName());
    }

    public void inlineMacro(String blockName,
            Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + inlineMacroProcessor.getName());
        this.asciidoctorModule.inline_macro(
                RubyUtils.toSymbol(rubyRuntime, blockName),
                inlineMacroProcessor.getSimpleName());
    }
    
    
}
