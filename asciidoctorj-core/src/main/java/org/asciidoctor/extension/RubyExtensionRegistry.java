package org.asciidoctor.extension;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;

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
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "preprocessor", rubyRuntime.newString(preprocessor));
        return this;
    }

    public RubyExtensionRegistry postprocessor(String postprocessor) {
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "postprocessor", rubyRuntime.newString(postprocessor));
        return this;
    }

    public RubyExtensionRegistry docinfoProcessor(String docinfoProcessor) {
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "docinfo_processor", rubyRuntime.newString(docinfoProcessor));
        return this;
    }

    public RubyExtensionRegistry includeProcessor(String includeProcessor) {
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "include_processor", rubyRuntime.newString(includeProcessor));
        return this;
    }

    public RubyExtensionRegistry treeprocessor(String treeProcessor) {
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "treeprocessor", rubyRuntime.newString(treeProcessor));
        return this;
    }

    public RubyExtensionRegistry block(String blockName, String blockProcessor) {
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "block_processor", rubyRuntime.newString(blockProcessor), RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    public RubyExtensionRegistry blockMacro(String blockName, String blockMacroProcessor) {
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "block_macro", rubyRuntime.newString(blockMacroProcessor), RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    public RubyExtensionRegistry inlineMacro(String blockName, String inlineMacroProcessor) {
        rubyRuntime.getModule("AsciidoctorModule")
            .callMethod( "inline_macro", rubyRuntime.newString(inlineMacroProcessor), RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }
}