package org.asciidoctor.extension;

import org.asciidoctor.internal.AsciidoctorModule;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;

public class JavaExtensionRegistry {

    private AsciidoctorModule asciidoctorModule;
    private Ruby rubyRuntime;
    
    public JavaExtensionRegistry(AsciidoctorModule asciidoctorModule, Ruby rubyRuntime) {
        super();
        this.asciidoctorModule = asciidoctorModule;
        this.rubyRuntime = rubyRuntime;
    }
    
    public void preprocessor(Class<? extends Preprocessor> preprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + getImportLine(preprocessor));
        this.asciidoctorModule.preprocessor(preprocessor.getSimpleName());
    }

    public void preprocessor(Preprocessor preprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + getImportLine(preprocessor.getClass()));
        this.asciidoctorModule.preprocessor(preprocessor);
    }
    
    public void postprocessor(Class<? extends Postprocessor> postprocesor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + getImportLine(postprocesor));
        this.asciidoctorModule.postprocessor(postprocesor.getSimpleName());
    }
    
    public void postprocessor(Postprocessor postprocesor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + getImportLine(postprocesor.getClass()));
        this.asciidoctorModule.postprocessor(postprocesor);
    }

    public void includeProcessor(
            Class<? extends IncludeProcessor> includeProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(includeProcessor));
        this.asciidoctorModule.include_processor(includeProcessor
                .getSimpleName());
    }

    public void includeProcessor(IncludeProcessor includeProcessor) {
    	this.rubyRuntime.evalScriptlet("java_import " + getImportLine(includeProcessor.getClass()));
    	this.asciidoctorModule.include_processor(includeProcessor);
    }
    
    public void treeprocessor(Treeprocessor treeprocessor) {
    	this.rubyRuntime.evalScriptlet("java_import " + getImportLine(treeprocessor.getClass()));
    	this.asciidoctorModule.treeprocessor(treeprocessor);
    }
    
    public void treeprocessor(Class<? extends Treeprocessor> treeProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime
                .evalScriptlet("java_import " + getImportLine(treeProcessor));
        this.asciidoctorModule.treeprocessor(treeProcessor.getSimpleName());
    }

    public void block(String blockName,
            Class<? extends BlockProcessor> blockProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(blockProcessor));
        /*try {
            blockProcessor.getMethod("setup", Ruby.class).invoke(null, this.rubyRuntime);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke setup method on block processor class: " + blockProcessor, e);
        }*/
        
        this.asciidoctorModule.block_processor(
                blockProcessor.getSimpleName(),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }

    public void block(BlockProcessor blockProcessor) {
        block(blockProcessor.getName(), blockProcessor);
    }
    
    public void block(String blockName,
            BlockProcessor blockProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(blockProcessor.getClass()));
        /*try {
            blockProcessor.getMethod("setup", Ruby.class).invoke(null, this.rubyRuntime);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke setup method on block processor class: " + blockProcessor, e);
        }*/
        
        this.asciidoctorModule.block_processor(
                blockProcessor,
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }

    public void blockMacro(String blockName,
            Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(blockMacroProcessor));
        this.asciidoctorModule.block_macro(
                blockMacroProcessor.getSimpleName(),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }

    public void blockMacro(String blockName,
                           BlockMacroProcessor blockMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(blockMacroProcessor.getClass()));
        this.asciidoctorModule.block_macro(
                blockMacroProcessor,
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }

    public void inlineMacro(String blockName,
            Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(inlineMacroProcessor));
        this.asciidoctorModule.inline_macro(
                RubyUtils.toSymbol(rubyRuntime, blockName),
                inlineMacroProcessor.getSimpleName());
    }
    
    private String getImportLine(Class<?> extensionClass) {
        return extensionClass.getName().replace("$", "::");
    }
    
}
