package org.asciidoctor.extension;

import java.io.InputStream;

import org.asciidoctor.internal.AsciidoctorModule;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;

public class RubyExtensionRegistry {

    private AsciidoctorModule asciidoctorModule;
    private Ruby rubyRuntime;

    public RubyExtensionRegistry(AsciidoctorModule asciidoctorModule,
            Ruby rubyRuntime) {
        super();
        this.asciidoctorModule = asciidoctorModule;
        this.rubyRuntime = rubyRuntime;
    }

    public RubyExtensionRegistry requireLibrary(String requiredLibrary) {
        RubyUtils.requireLibrary(rubyRuntime, requiredLibrary);
        return this;
    }
    
    public RubyExtensionRegistry loadClass(InputStream rubyClassStream) {
        RubyUtils.loadRubyClass(rubyRuntime, rubyClassStream);
        return this;
    }
    
    public RubyExtensionRegistry preprocessor(String preprocessor) {
        this.asciidoctorModule.preprocessor(preprocessor);
        return this;
    }

    public RubyExtensionRegistry preprocessor(String preprocessor, String registrationName) {
        this.asciidoctorModule.preprocessor(preprocessor, registrationName);
        return this;
    }

    public RubyExtensionRegistry postprocessor(String postprocessor) {
        this.asciidoctorModule.postprocessor(postprocessor);
        return this;
    }

    public RubyExtensionRegistry postprocessor(String postprocessor, String registrationName) {
        this.asciidoctorModule.postprocessor(postprocessor, registrationName);
        return this;
    }

    public RubyExtensionRegistry docinfoProcessor(String docinfoProcessor) {
        this.asciidoctorModule.docinfo_processor(docinfoProcessor);
        return this;
    }

    public RubyExtensionRegistry docinfoProcessor(String docinfoProcessor, String registrationName) {
        this.asciidoctorModule.docinfo_processor(docinfoProcessor, registrationName);
        return this;
    }

    public RubyExtensionRegistry includeProcessor(String includeProcessor) {
        this.asciidoctorModule.include_processor(includeProcessor);
        return this;
    }

    public RubyExtensionRegistry includeProcessor(String includeProcessor, String registrationName) {
        this.asciidoctorModule.include_processor(includeProcessor, registrationName);
        return this;
    }

    public RubyExtensionRegistry treeprocessor(String treeProcessor) {
        this.asciidoctorModule.treeprocessor(treeProcessor);
        return this;
    }

    public RubyExtensionRegistry treeprocessor(String treeProcessor, String registrationName) {
        this.asciidoctorModule.treeprocessor(treeProcessor, registrationName);
        return this;
    }

    public RubyExtensionRegistry block(String blockName, String blockProcessor) {
        this.asciidoctorModule.block_processor(
                blockProcessor, RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    public RubyExtensionRegistry block(String blockName, String blockProcessor, String registrationName) {
        this.asciidoctorModule.block_processor(
                blockProcessor, RubyUtils.toSymbol(rubyRuntime, blockName), registrationName);
        return this;
    }

    public RubyExtensionRegistry blockMacro(String blockName, String blockMacroProcessor) {

        this.asciidoctorModule.block_macro(
                blockMacroProcessor, RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    public RubyExtensionRegistry blockMacro(String blockName, String blockMacroProcessor, String registrationName) {

        this.asciidoctorModule.block_macro(
                blockMacroProcessor, RubyUtils.toSymbol(rubyRuntime, blockName), registrationName);
        return this;
    }

    public RubyExtensionRegistry inlineMacro(String blockName, String inlineMacroProcessor) {

        this.asciidoctorModule.inline_macro(
                inlineMacroProcessor, RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    public RubyExtensionRegistry inlineMacro(String blockName, String inlineMacroProcessor, String registrationName) {

        this.asciidoctorModule.inline_macro(
                inlineMacroProcessor, RubyUtils.toSymbol(rubyRuntime, blockName), registrationName);
        return this;
    }

}