package org.asciidoctor.core.api.extension;

import org.asciidoctor.core.api.CoreAsciidoctor;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.extension.JavaExtensionRegistry;

/**
 * The {@link JavaExtensionRegistry} of the AsciidoctorJ API: each processor is registered immediately with the
 * Asciidoctor instance (in a group of its own).
 */
public class JavaExtensionRegistryImpl implements JavaExtensionRegistry {
    private final CoreAsciidoctor asciidoctor;

    private final Registrations registrations;

    public JavaExtensionRegistryImpl(CoreAsciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
        this.registrations = new Registrations(asciidoctor);
    }

    private void register(java.util.function.Consumer<org.asciidoctor.core.extension.Registry> registration) {
        asciidoctor.registerExtensionGroup(null, registration::accept);
    }

    @Override
    public JavaExtensionRegistry docinfoProcessor(Class<? extends DocinfoProcessor> docinfoProcessor) {
        register(registrations.docinfoProcessor(docinfoProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry docinfoProcessor(DocinfoProcessor docinfoProcessor) {
        register(registrations.docinfoProcessor(docinfoProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry docinfoProcessor(String docinfoProcessor) {
        register(registrations.docinfoProcessor(docinfoProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry preprocessor(Class<? extends Preprocessor> preprocessor) {
        register(registrations.preprocessor(preprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry preprocessor(Preprocessor preprocessor) {
        register(registrations.preprocessor(preprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry preprocessor(String preprocessor) {
        register(registrations.preprocessor(preprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry postprocessor(String postprocessor) {
        register(registrations.postprocessor(postprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry postprocessor(Class<? extends Postprocessor> postprocessor) {
        register(registrations.postprocessor(postprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry postprocessor(Postprocessor postprocessor) {
        register(registrations.postprocessor(postprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry includeProcessor(String includeProcessor) {
        register(registrations.includeProcessor(includeProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry includeProcessor(Class<? extends IncludeProcessor> includeProcessor) {
        register(registrations.includeProcessor(includeProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry includeProcessor(IncludeProcessor includeProcessor) {
        register(registrations.includeProcessor(includeProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry treeprocessor(Treeprocessor treeprocessor) {
        register(registrations.treeprocessor(treeprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry treeprocessor(Class<? extends Treeprocessor> treeprocessor) {
        register(registrations.treeprocessor(treeprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry treeprocessor(String treeprocessor) {
        register(registrations.treeprocessor(treeprocessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry block(String blockName, String blockProcessor) {
        register(registrations.block(blockName, blockProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry block(String blockProcessor) {
        return block(ProcessorProxies.loadClass(blockProcessor, BlockProcessor.class));
    }

    @Override
    public JavaExtensionRegistry block(String blockName, Class<? extends BlockProcessor> blockProcessor) {
        register(registrations.block(blockName, blockProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry block(Class<? extends BlockProcessor> blockProcessor) {
        // NOTE like AsciidoctorJ, the class is registered for each name declared by a Name annotation
        for (String name : Registrations.names(blockProcessor)) {
            register(registrations.block(name, blockProcessor));
        }
        return this;
    }

    @Override
    public JavaExtensionRegistry block(BlockProcessor blockProcessor) {
        register(registrations.block(null, blockProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry block(String blockName, BlockProcessor blockProcessor) {
        register(registrations.block(blockName, blockProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String blockName, Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        register(registrations.blockMacro(blockName, blockMacroProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        // NOTE like AsciidoctorJ, the class is registered for each name declared by a Name annotation
        for (String name : Registrations.names(blockMacroProcessor)) {
            register(registrations.blockMacro(name, blockMacroProcessor));
        }
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String blockName, String blockMacroProcessor) {
        register(registrations.blockMacro(blockName, blockMacroProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String blockMacroProcessor) {
        return blockMacro(ProcessorProxies.loadClass(blockMacroProcessor, BlockMacroProcessor.class));
    }

    @Override
    public JavaExtensionRegistry blockMacro(BlockMacroProcessor blockMacroProcessor) {
        register(registrations.blockMacro(null, blockMacroProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry blockMacro(String blockName, BlockMacroProcessor blockMacroProcessor) {
        register(registrations.blockMacro(blockName, blockMacroProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
        register(registrations.inlineMacro(null, inlineMacroProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String macroName, InlineMacroProcessor inlineMacroProcessor) {
        register(registrations.inlineMacro(macroName, inlineMacroProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String macroName, Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        register(registrations.inlineMacro(macroName, inlineMacroProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        // NOTE like AsciidoctorJ, the class is registered for each name declared by a Name annotation
        for (String name : Registrations.names(inlineMacroProcessor)) {
            register(registrations.inlineMacro(name, inlineMacroProcessor));
        }
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String macroName, String inlineMacroProcessor) {
        register(registrations.inlineMacro(macroName, inlineMacroProcessor));
        return this;
    }

    @Override
    public JavaExtensionRegistry inlineMacro(String inlineMacroProcessor) {
        return inlineMacro(ProcessorProxies.loadClass(inlineMacroProcessor, InlineMacroProcessor.class));
    }
}
