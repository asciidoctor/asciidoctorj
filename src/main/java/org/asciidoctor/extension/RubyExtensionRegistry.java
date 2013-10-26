package org.asciidoctor.extension;

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

    public void preprocessor(String preprocessor) {
        this.asciidoctorModule.preprocessor(preprocessor);
    }

    public void postprocessor(String postprocesor) {
        this.asciidoctorModule.postprocessor(postprocesor);
    }

    public void includeProcessor(String includeProcessor) {
        this.asciidoctorModule.include_processor(includeProcessor);
    }

    public void treeprocessor(String treeProcessor) {
        this.asciidoctorModule.treeprocessor(treeProcessor);
    }

    public void block(String blockName, String blockProcessor) {
        this.asciidoctorModule.block_processor(
                RubyUtils.toSymbol(rubyRuntime, blockName), blockProcessor);
    }

    public void blockMacro(String blockName, String blockMacroProcessor) {

        this.asciidoctorModule
                .block_macro(RubyUtils.toSymbol(rubyRuntime, blockName),
                        blockMacroProcessor);
    }

    public void inlineMacro(String blockName, String inlineMacroProcessor) {

        this.asciidoctorModule.inline_macro(
                RubyUtils.toSymbol(rubyRuntime, blockName),
                inlineMacroProcessor);
    }

}