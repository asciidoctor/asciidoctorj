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

    public void docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, docInfoProcessor);
        this.asciidoctorModule.docinfo_processor(RubyUtils.toRubyClass(rubyRuntime, docInfoProcessor));
    }

    public void docinfoProcessor(DocinfoProcessor docInfoProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, docInfoProcessor.getClass());
        this.asciidoctorModule.docinfo_processor(docInfoProcessor);
    }

    public void docinfoProcessor(String docInfoProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, docInfoProcessor);
        this.asciidoctorModule.docinfo_processor(getClassName(docInfoProcessor));
    }

    public void preprocessor(Class<? extends Preprocessor> preprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, preprocessor);
        this.asciidoctorModule.preprocessor(RubyUtils.toRubyClass(rubyRuntime, preprocessor));
    }

    public void preprocessor(Preprocessor preprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, preprocessor.getClass());
        this.asciidoctorModule.preprocessor(preprocessor);
    }
    
    public void preprocessor(String preprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, preprocessor);
        this.asciidoctorModule.preprocessor(getClassName(preprocessor));
    }
    
    public void postprocessor(String postprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, postprocessor);
        this.asciidoctorModule.postprocessor(getClassName(postprocessor));
    }
    
    public void postprocessor(Class<? extends Postprocessor> postprocessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, postprocessor);
        this.asciidoctorModule.postprocessor(RubyUtils.toRubyClass(rubyRuntime, postprocessor));
    }
    
    public void postprocessor(Postprocessor postprocesor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, postprocesor.getClass());
        this.asciidoctorModule.postprocessor(postprocesor);
    }

    public void includeProcessor(String includeProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, includeProcessor);
        this.asciidoctorModule.include_processor(getClassName(includeProcessor));
    }
    
    public void includeProcessor(
            Class<? extends IncludeProcessor> includeProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, includeProcessor);
      this.asciidoctorModule.include_processor(RubyUtils.toRubyClass(rubyRuntime, includeProcessor));
    }

    public void includeProcessor(IncludeProcessor includeProcessor) {
        String importLine = getImportLine(includeProcessor.getClass());
        javaImport(rubyRuntime, importLine);
        this.asciidoctorModule.include_processor(includeProcessor);
    }
    
    public void treeprocessor(Treeprocessor treeprocessor) {
        javaImport(rubyRuntime, treeprocessor.getClass());
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
        javaImport(rubyRuntime, treeProcessor);
        this.asciidoctorModule.treeprocessor(getClassName(treeProcessor));
    }

    public void block(String blockName,
           String blockProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, blockProcessor);
        
        this.asciidoctorModule.block_processor(
                getClassName(blockProcessor),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }
    
    public void block(String blockName,
            Class<? extends BlockProcessor> blockProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, blockProcessor);
        
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
        javaImport(rubyRuntime, blockProcessor.getClass());
        
        this.asciidoctorModule.block_processor(
                blockProcessor,
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }

    public void blockMacro(String blockName,
            Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, blockMacroProcessor);
        this.asciidoctorModule.block_macro(
                blockMacroProcessor.getSimpleName(),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }

    public void blockMacro(String blockName,
            String blockMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, blockMacroProcessor);
        this.asciidoctorModule.block_macro(
                getClassName(blockMacroProcessor),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }
    
    public void blockMacro(BlockMacroProcessor blockMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, blockMacroProcessor.getClass());
        this.asciidoctorModule.block_macro(
                blockMacroProcessor,
                RubyUtils.toSymbol(rubyRuntime, blockMacroProcessor.getName()));
    }

    public void inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, inlineMacroProcessor.getClass());
        
        this.asciidoctorModule.inline_macro(
        		inlineMacroProcessor,
                RubyUtils.toSymbol(rubyRuntime, inlineMacroProcessor.getName()));
    }
    
    public void inlineMacro(String blockName,
            Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        // this may change in future to external class to deal with dynamic
        // imports
        javaImport(rubyRuntime, inlineMacroProcessor);
        
        this.asciidoctorModule.inline_macro(
        		RubyUtils.toRubyClass(rubyRuntime, inlineMacroProcessor),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }
    
    public void inlineMacro(String blockName, String inlineMacroProcessor) {
        // this may change in future to external class to deal with dynamic imports
        javaImport(this.rubyRuntime, inlineMacroProcessor);
        
        this.asciidoctorModule.inline_macro(
        		getClassName(inlineMacroProcessor),
                RubyUtils.toSymbol(rubyRuntime, blockName));
    }

    private void javaImport(Ruby ruby, Class<?> clazz) {
      ruby.evalScriptlet(String.format("java_import '%s'", getImportLine(clazz)));
    }
  
    private void javaImport(Ruby ruby, String className) {
      ruby.evalScriptlet(String.format("java_import '%s'", className));
    }

    private String getImportLine(Class<?> extensionClass) {
        int dollarPosition = -1;
        String className = extensionClass.getName();
        if ((dollarPosition = className.indexOf("$")) != -1) {
            className = className.substring(0, dollarPosition);
        }
        return className;
    }
    
    private String getClassName(String clazz) {
    	return clazz.substring(clazz.lastIndexOf(".")+1);
    }
    
}
