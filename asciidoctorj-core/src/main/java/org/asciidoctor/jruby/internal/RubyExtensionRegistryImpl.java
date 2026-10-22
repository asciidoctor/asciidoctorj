package org.asciidoctor.jruby.internal;

import org.asciidoctor.extension.RubyExtensionRegistry;
import org.jruby.Ruby;
import org.jruby.RubyModule;

import java.io.InputStream;

public class RubyExtensionRegistryImpl implements RubyExtensionRegistry {


    private Ruby rubyRuntime;

    public RubyExtensionRegistryImpl(final Ruby rubyRuntime) {
        this.rubyRuntime = rubyRuntime;
    }

    @Override
    public RubyExtensionRegistryImpl requireLibrary(String requiredLibrary) {
        RubyUtils.requireLibrary(rubyRuntime, requiredLibrary);
        return this;
    }
    
    @Override
    public RubyExtensionRegistryImpl loadClass(InputStream rubyClassStream) {
        RubyUtils.loadRubyClass(rubyRuntime, rubyClassStream);
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl preprocessor(String preprocessor) {
        getAsciidoctorModule().callMethod( "preprocessor", rubyRuntime.newString(preprocessor));
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl postprocessor(String postprocessor) {
        getAsciidoctorModule().callMethod( "postprocessor", rubyRuntime.newString(postprocessor));
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl docinfoProcessor(String docinfoProcessor) {
        getAsciidoctorModule().callMethod( "docinfo_processor", rubyRuntime.newString(docinfoProcessor));
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl includeProcessor(String includeProcessor) {
        getAsciidoctorModule().callMethod( "include_processor", rubyRuntime.newString(includeProcessor));
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl treeprocessor(String treeProcessor) {
        getAsciidoctorModule().callMethod( "treeprocessor", rubyRuntime.newString(treeProcessor));
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl block(String blockName, String blockProcessor) {
        getAsciidoctorModule().callMethod( "block_processor", rubyRuntime.newString(blockProcessor), RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl block(String blockProcessor) {
        getAsciidoctorModule().callMethod( "block_processor", rubyRuntime.newString(blockProcessor));
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl blockMacro(String blockName, String blockMacroProcessor) {
        getAsciidoctorModule().callMethod( "block_macro", rubyRuntime.newString(blockMacroProcessor), RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl blockMacro(String blockMacroProcessor) {
        getAsciidoctorModule().callMethod( "block_macro", rubyRuntime.newString(blockMacroProcessor));
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl inlineMacro(String blockName, String inlineMacroProcessor) {
        getAsciidoctorModule().callMethod( "inline_macro", rubyRuntime.newString(inlineMacroProcessor), RubyUtils.toSymbol(rubyRuntime, blockName));
        return this;
    }

    @Override
    public RubyExtensionRegistryImpl inlineMacro(String inlineMacroProcessor) {
        getAsciidoctorModule().callMethod( "inline_macro", rubyRuntime.newString(inlineMacroProcessor));
        return this;
    }

    private RubyModule getAsciidoctorModule() {
        return rubyRuntime.getModule("AsciidoctorModule");
    }
}