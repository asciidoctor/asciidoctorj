package org.asciidoctor.core.api.extension;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.asciidoctor.core.api.CoreAsciidoctor;
import org.asciidoctor.core.extension.Registry;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.extension.ExtensionGroup;

/**
 * The {@link ExtensionGroup} of the AsciidoctorJ API: the processors are registered with the Asciidoctor instance
 * together when the group is registered, and unregistered together.
 */
public class ExtensionGroupImpl implements ExtensionGroup {
    private final String groupName;
    private final CoreAsciidoctor asciidoctor;
    private final List<Consumer<Registry>> groupRegistrations = new ArrayList<>();
    private final Registrations registrations;

    public ExtensionGroupImpl(String groupName, CoreAsciidoctor asciidoctor) {
        this.groupName = groupName;
        this.asciidoctor = asciidoctor;
        this.registrations = new Registrations(asciidoctor);
    }

    private void add(Consumer<Registry> registration) {
        groupRegistrations.add(registration);
    }

    @Override
    public void register() {
        List<Consumer<Registry>> group = List.copyOf(groupRegistrations);
        asciidoctor.registerExtensionGroup(groupName, registry -> group.forEach(registration -> registration.accept(registry)));
    }

    @Override
    public void unregister() {
        asciidoctor.unregisterExtensionGroup(groupName);
    }

    @Override
    public ExtensionGroup docinfoProcessor(Class<? extends DocinfoProcessor> docinfoProcessor) {
        add(registrations.docinfoProcessor(docinfoProcessor));
        return this;
    }

    @Override
    public ExtensionGroup docinfoProcessor(DocinfoProcessor docinfoProcessor) {
        add(registrations.docinfoProcessor(docinfoProcessor));
        return this;
    }

    @Override
    public ExtensionGroup docinfoProcessor(String docinfoProcessor) {
        add(registrations.docinfoProcessor(docinfoProcessor));
        return this;
    }

    @Override
    public ExtensionGroup preprocessor(Class<? extends Preprocessor> preprocessor) {
        add(registrations.preprocessor(preprocessor));
        return this;
    }

    @Override
    public ExtensionGroup preprocessor(Preprocessor preprocessor) {
        add(registrations.preprocessor(preprocessor));
        return this;
    }

    @Override
    public ExtensionGroup preprocessor(String preprocessor) {
        add(registrations.preprocessor(preprocessor));
        return this;
    }

    @Override
    public ExtensionGroup postprocessor(String postprocessor) {
        add(registrations.postprocessor(postprocessor));
        return this;
    }

    @Override
    public ExtensionGroup postprocessor(Class<? extends Postprocessor> postprocessor) {
        add(registrations.postprocessor(postprocessor));
        return this;
    }

    @Override
    public ExtensionGroup postprocessor(Postprocessor postprocessor) {
        add(registrations.postprocessor(postprocessor));
        return this;
    }

    @Override
    public ExtensionGroup includeProcessor(String includeProcessor) {
        add(registrations.includeProcessor(includeProcessor));
        return this;
    }

    @Override
    public ExtensionGroup includeProcessor(Class<? extends IncludeProcessor> includeProcessor) {
        add(registrations.includeProcessor(includeProcessor));
        return this;
    }

    @Override
    public ExtensionGroup includeProcessor(IncludeProcessor includeProcessor) {
        add(registrations.includeProcessor(includeProcessor));
        return this;
    }

    @Override
    public ExtensionGroup treeprocessor(Treeprocessor treeprocessor) {
        add(registrations.treeprocessor(treeprocessor));
        return this;
    }

    @Override
    public ExtensionGroup treeprocessor(Class<? extends Treeprocessor> treeprocessor) {
        add(registrations.treeprocessor(treeprocessor));
        return this;
    }

    @Override
    public ExtensionGroup treeprocessor(String treeprocessor) {
        add(registrations.treeprocessor(treeprocessor));
        return this;
    }

    @Override
    public ExtensionGroup block(String blockName, String blockProcessor) {
        add(registrations.block(blockName, blockProcessor));
        return this;
    }

    @Override
    public ExtensionGroup block(String blockProcessor) {
        add(registrations.block(null, blockProcessor));
        return this;
    }

    @Override
    public ExtensionGroup block(String blockName, Class<? extends BlockProcessor> blockProcessor) {
        add(registrations.block(blockName, blockProcessor));
        return this;
    }

    @Override
    public ExtensionGroup block(Class<? extends BlockProcessor> blockProcessor) {
        add(registrations.block(null, blockProcessor));
        return this;
    }

    @Override
    public ExtensionGroup block(BlockProcessor blockProcessor) {
        add(registrations.block(null, blockProcessor));
        return this;
    }

    @Override
    public ExtensionGroup block(String blockName, BlockProcessor blockProcessor) {
        add(registrations.block(blockName, blockProcessor));
        return this;
    }

    @Override
    public ExtensionGroup blockMacro(String blockName, Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        add(registrations.blockMacro(blockName, blockMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        add(registrations.blockMacro(null, blockMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup blockMacro(String blockName, String blockMacroProcessor) {
        add(registrations.blockMacro(blockName, blockMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup blockMacro(String blockMacroProcessor) {
        add(registrations.blockMacro(null, blockMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup blockMacro(BlockMacroProcessor blockMacroProcessor) {
        add(registrations.blockMacro(null, blockMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup blockMacro(String blockName, BlockMacroProcessor blockMacroProcessor) {
        add(registrations.blockMacro(blockName, blockMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup inlineMacro(InlineMacroProcessor inlineMacroProcessor) {
        add(registrations.inlineMacro(null, inlineMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup inlineMacro(String macroName, InlineMacroProcessor inlineMacroProcessor) {
        add(registrations.inlineMacro(macroName, inlineMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup inlineMacro(String macroName, Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        add(registrations.inlineMacro(macroName, inlineMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        add(registrations.inlineMacro(null, inlineMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup inlineMacro(String macroName, String inlineMacroProcessor) {
        add(registrations.inlineMacro(macroName, inlineMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup inlineMacro(String inlineMacroProcessor) {
        add(registrations.inlineMacro(null, inlineMacroProcessor));
        return this;
    }

    @Override
    public ExtensionGroup requireRubyLibrary(String requiredLibrary) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup loadRubyClass(InputStream rubyClassStream) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyPreprocessor(String preprocessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyPostprocessor(String postprocessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyDocinfoProcessor(String docinfoProcessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyIncludeProcessor(String includeProcessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyTreeprocessor(String treeProcessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyBlock(String blockName, String blockProcessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyBlock(String blockProcessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyBlockMacro(String blockName, String blockMacroProcessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyBlockMacro(String blockMacroProcessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyInlineMacro(String macroName, String inlineMacroProcessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }

    @Override
    public ExtensionGroup rubyInlineMacro(String inlineMacroProcessor) {
        throw RubyExtensionRegistryImpl.unsupported();
    }
}
