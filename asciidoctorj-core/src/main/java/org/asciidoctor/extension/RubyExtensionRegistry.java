package org.asciidoctor.extension;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyModule;

import java.io.InputStream;

public class RubyExtensionRegistry {


    private Ruby rubyRuntime;

    public RubyExtensionRegistry(final Ruby rubyRuntime) {
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
        getAsciidoctorModule().callMethod( "preprocessor", rubyRuntime.newString(preprocessor));
        return this;
    }

    public RubyExtensionRegistry postprocessor(String postprocessor) {
        getAsciidoctorModule().callMethod( "postprocessor", rubyRuntime.newString(postprocessor));
        return this;
    }

    public RubyExtensionRegistry docinfoProcessor(String docinfoProcessor) {
        getAsciidoctorModule().callMethod( "docinfo_processor", rubyRuntime.newString(docinfoProcessor));
        return this;
    }

    public RubyExtensionRegistry includeProcessor(String includeProcessor) {
        getAsciidoctorModule().callMethod( "include_processor", rubyRuntime.newString(includeProcessor));
        return this;
    }

    public RubyExtensionRegistry treeprocessor(String treeProcessor) {
        getAsciidoctorModule().callMethod( "treeprocessor", rubyRuntime.newString(treeProcessor));
        return this;
    }

    public RubyExtensionRegistry block(String blockName, String blockProcessor) {
        getAsciidoctorModule().callMethod( "block_processor", rubyRuntime.newString(blockProcessor), RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    public RubyExtensionRegistry block(String blockProcessor) {
        getAsciidoctorModule().callMethod( "block_processor", rubyRuntime.newString(blockProcessor));
        return this;
    }

    public RubyExtensionRegistry blockMacro(String blockName, String blockMacroProcessor) {
        getAsciidoctorModule().callMethod( "block_macro", rubyRuntime.newString(blockMacroProcessor), RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    public RubyExtensionRegistry blockMacro(String blockMacroProcessor) {
        getAsciidoctorModule().callMethod( "block_macro", rubyRuntime.newString(blockMacroProcessor));
        return this;
    }

    public RubyExtensionRegistry inlineMacro(String blockName, String inlineMacroProcessor) {
        getAsciidoctorModule().callMethod( "inline_macro", rubyRuntime.newString(inlineMacroProcessor), RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    public RubyExtensionRegistry inlineMacro(String inlineMacroProcessor) {
        getAsciidoctorModule().callMethod( "inline_macro", rubyRuntime.newString(inlineMacroProcessor));
        return this;
    }

    private RubyModule getAsciidoctorModule() {
        return rubyRuntime.getModule("AsciidoctorModule");
    }
}