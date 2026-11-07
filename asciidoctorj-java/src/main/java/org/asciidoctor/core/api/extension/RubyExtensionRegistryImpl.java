package org.asciidoctor.core.api.extension;

import java.io.InputStream;

import org.asciidoctor.extension.RubyExtensionRegistry;

/**
 * The {@link RubyExtensionRegistry} of the AsciidoctorJ API, which is not supported by this implementation since
 * there's no Ruby runtime: each method throws an {@link UnsupportedOperationException}.
 */
public class RubyExtensionRegistryImpl implements RubyExtensionRegistry {

    static UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Ruby extensions are not supported by this implementation of "
                + "AsciidoctorJ (there's no Ruby runtime)");
    }

    @Override
    public RubyExtensionRegistry requireLibrary(String requiredLibrary) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry loadClass(InputStream rubyClassStream) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry preprocessor(String preprocessor) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry postprocessor(String postprocessor) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry docinfoProcessor(String docinfoProcessor) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry includeProcessor(String includeProcessor) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry treeprocessor(String treeProcessor) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry block(String blockName, String blockProcessor) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry block(String blockProcessor) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry blockMacro(String blockName, String blockMacroProcessor) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry blockMacro(String blockMacroProcessor) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry inlineMacro(String blockName, String inlineMacroProcessor) {
        throw unsupported();
    }

    @Override
    public RubyExtensionRegistry inlineMacro(String inlineMacroProcessor) {
        throw unsupported();
    }
}
