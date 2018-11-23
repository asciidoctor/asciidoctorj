package org.asciidoctor.extension;

public interface JavaExtensionRegistry {

    JavaExtensionRegistry docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor);

    JavaExtensionRegistry docinfoProcessor(DocinfoProcessor docInfoProcessor);

    JavaExtensionRegistry docinfoProcessor(String docInfoProcessor);

    JavaExtensionRegistry preprocessor(Class<? extends Preprocessor> preprocessor);

    JavaExtensionRegistry preprocessor(Preprocessor preprocessor);

    JavaExtensionRegistry preprocessor(String preprocessor);

    JavaExtensionRegistry postprocessor(String postprocessor);

    JavaExtensionRegistry postprocessor(Class<? extends Postprocessor> postprocessor);

    JavaExtensionRegistry postprocessor(Postprocessor postprocessor);

    JavaExtensionRegistry includeProcessor(String includeProcessor);

    JavaExtensionRegistry includeProcessor(Class<? extends IncludeProcessor> includeProcessor);

    JavaExtensionRegistry includeProcessor(IncludeProcessor includeProcessor);

    JavaExtensionRegistry treeprocessor(Treeprocessor treeprocessor);

    JavaExtensionRegistry treeprocessor(Class<? extends Treeprocessor> abstractTreeProcessor);

    JavaExtensionRegistry treeprocessor(String treeProcessor);

    JavaExtensionRegistry block(String blockName, String blockProcessor);

    JavaExtensionRegistry block(String blockProcessor);

    JavaExtensionRegistry block(String blockName, Class<? extends BlockProcessor> blockProcessor);

    JavaExtensionRegistry block(Class<? extends BlockProcessor> blockProcessor);

    JavaExtensionRegistry block(BlockProcessor blockProcessor);

    JavaExtensionRegistry block(String blockName, BlockProcessor blockProcessor);

    JavaExtensionRegistry blockMacro(String blockName, Class<? extends BlockMacroProcessor> blockMacroProcessor);

    JavaExtensionRegistry blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor);

    JavaExtensionRegistry blockMacro(String blockName, String blockMacroProcessor);


    JavaExtensionRegistry blockMacro(String blockMacroProcessor);

    JavaExtensionRegistry blockMacro(BlockMacroProcessor blockMacroProcessor);

    JavaExtensionRegistry blockMacro(String blockName,
                                     BlockMacroProcessor blockMacroProcessor);

    JavaExtensionRegistry inlineMacro(InlineMacroProcessor inlineMacroProcessor);

    JavaExtensionRegistry inlineMacro(String macroName, InlineMacroProcessor inlineMacroProcessor);

    JavaExtensionRegistry inlineMacro(String macroName, Class<? extends InlineMacroProcessor> inlineMacroProcessor);

    JavaExtensionRegistry inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor);

    JavaExtensionRegistry inlineMacro(String blockName, String inlineMacroProcessor);

    JavaExtensionRegistry inlineMacro(String inlineMacroProcessor);
}
