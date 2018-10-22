package org.asciidoctor.extension;

import org.asciidoctor.internal.RubyExtensionRegistryImpl;

import java.io.InputStream;

public interface RubyExtensionRegistry {
    RubyExtensionRegistryImpl requireLibrary(String requiredLibrary);

    RubyExtensionRegistryImpl loadClass(InputStream rubyClassStream);

    RubyExtensionRegistryImpl preprocessor(String preprocessor);

    RubyExtensionRegistryImpl postprocessor(String postprocessor);

    RubyExtensionRegistryImpl docinfoProcessor(String docinfoProcessor);

    RubyExtensionRegistryImpl includeProcessor(String includeProcessor);

    RubyExtensionRegistryImpl treeprocessor(String treeProcessor);

    RubyExtensionRegistryImpl block(String blockName, String blockProcessor);

    RubyExtensionRegistryImpl block(String blockProcessor);

    RubyExtensionRegistryImpl blockMacro(String blockName, String blockMacroProcessor);

    RubyExtensionRegistryImpl blockMacro(String blockMacroProcessor);

    RubyExtensionRegistryImpl inlineMacro(String blockName, String inlineMacroProcessor);

    RubyExtensionRegistryImpl inlineMacro(String inlineMacroProcessor);
}
