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
        this.asciidoctorModule.preprocessor(RubyUtils.toRubyClass(rubyRuntime, preprocessor));
    }

    public void preprocessor(Preprocessor preprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + getImportLine(preprocessor.getClass()));
        this.asciidoctorModule.preprocessor(preprocessor);
    }
    
    public void preprocessor(String preprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + preprocessor);
        this.asciidoctorModule.preprocessor(getClassName(preprocessor));
    }
    
    public void postprocessor(String postprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + postprocessor);
        this.asciidoctorModule.postprocessor(getClassName(postprocessor));
    }
    
    public void postprocessor(Class<? extends Postprocessor> postprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + getImportLine(postprocessor));
        this.asciidoctorModule.postprocessor(RubyUtils.toRubyClass(rubyRuntime, postprocessor));
    }
    
    public void postprocessor(Postprocessor postprocesor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import " + getImportLine(postprocesor.getClass()));
        this.asciidoctorModule.postprocessor(postprocesor);
    }

    public void includeProcessor(
            String includeProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + includeProcessor);
        this.asciidoctorModule.include_processor(getClassName(includeProcessor));
    }
    
    public void includeProcessor(
            Class<? extends IncludeProcessor> includeProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(includeProcessor));
        this.asciidoctorModule.include_processor(RubyUtils.toRubyClass(rubyRuntime, includeProcessor));
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
        this.asciidoctorModule.treeprocessor(RubyUtils.toRubyClass(rubyRuntime, treeProcessor));
    }
    
    public void treeprocessor(String treeProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime
                .evalScriptlet("java_import " + treeProcessor);
        this.asciidoctorModule.treeprocessor(getClassName(treeProcessor));
    }

    public void block(String blockName,
           String blockProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
               + blockProcessor);
        
        this.asciidoctorModule.block_processor(
                getClassName(blockProcessor),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }
    
    public void block(String blockName,
            Class<? extends BlockProcessor> blockProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(blockProcessor));
        
        this.asciidoctorModule.block_processor(
                RubyUtils.toRubyClass(rubyRuntime, blockProcessor),
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
            String blockMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + blockMacroProcessor);
        this.asciidoctorModule.block_macro(
                getClassName(blockMacroProcessor),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }
    
    public void blockMacro(
                           BlockMacroProcessor blockMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(blockMacroProcessor.getClass()));
        this.asciidoctorModule.block_macro(
                blockMacroProcessor,
                RubyUtils.toSymbol(rubyRuntime, blockMacroProcessor.getName()));
    }

    public void inlineMacro(
           InlineMacroProcessor inlineMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(inlineMacroProcessor.getClass()));
        
        this.asciidoctorModule.inline_macro(
        		inlineMacroProcessor,
                RubyUtils.toSymbol(rubyRuntime, inlineMacroProcessor.getName()));
    }
    
    public void inlineMacro(String blockName,
            Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + getImportLine(inlineMacroProcessor));
        
        this.asciidoctorModule.inline_macro(
        		RubyUtils.toRubyClass(rubyRuntime, inlineMacroProcessor),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }
    
    public void inlineMacro(String blockName,
            String inlineMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        this.rubyRuntime.evalScriptlet("java_import "
                + inlineMacroProcessor);
        
        this.asciidoctorModule.inline_macro(
        		getClassName(inlineMacroProcessor),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }
    
    private String getImportLine(Class<?> extensionClass) {
        return extensionClass.getName().replace("$", "::");
    }
    
    private String getClassName(String clazz) {
    	return clazz.substring(clazz.lastIndexOf(".")+1);
    }
    
}
