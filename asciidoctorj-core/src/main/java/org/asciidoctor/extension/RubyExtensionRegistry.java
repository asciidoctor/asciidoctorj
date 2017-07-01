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
    
    public ExtensionRegistration preprocessor(String preprocessor) {
        final ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.preprocessor(registration.getName(rubyRuntime), preprocessor);
        return registration;
    }

    public ExtensionRegistration postprocessor(String postprocesor) {
        final ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.postprocessor(registration.getName(rubyRuntime), postprocesor);
        return registration;
    }

    public ExtensionRegistration docinfoProcessor(String docinfoProcessor) {
        final ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.docinfo_processor(registration.getName(rubyRuntime), docinfoProcessor);
        return registration;
    }

    public ExtensionRegistration includeProcessor(String includeProcessor) {
        final ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.include_processor(registration.getName(rubyRuntime), includeProcessor);
        return registration;
    }

    public ExtensionRegistration treeprocessor(String treeProcessor) {
        final ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.treeprocessor(registration.getName(rubyRuntime), treeProcessor);
        return registration;
    }

    public ExtensionRegistration block(String blockName, String blockProcessor) {
        final ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.block_processor(registration.getName(rubyRuntime),
            blockProcessor, RubyUtils.toSymbol(rubyRuntime, blockName));
        return registration;
    }

    public ExtensionRegistration blockMacro(String blockName, String blockMacroProcessor) {

        final ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.block_macro(registration.getName(rubyRuntime),
            blockMacroProcessor, RubyUtils.toSymbol(rubyRuntime, blockName));
        return registration;
    }

    public ExtensionRegistration inlineMacro(String blockName, String inlineMacroProcessor) {

        final ExtensionRegistration registration = new ExtensionRegistration();
        this.asciidoctorModule.inline_macro(registration.getName(rubyRuntime),
            inlineMacroProcessor, RubyUtils.toSymbol(rubyRuntime, blockName));
        return registration;
    }

}