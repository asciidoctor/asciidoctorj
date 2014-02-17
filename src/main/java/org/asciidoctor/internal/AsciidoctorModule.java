package org.asciidoctor.internal;

import java.util.Map;

import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.*;


public interface AsciidoctorModule {

    void preprocessor(String preprocessorClassName);
	void preprocessor(Preprocessor preprocessor);
    void postprocessor(String postprocessorClassName);
    void postprocessor(Postprocessor postprocessor);
    void block_processor(String blockClassName, Object blockName);
    void block_processor(Class<BlockProcessor> blockClass, Object blockName);
    void block_processor(BlockProcessor blockInstance, Object blockName);
    void block_macro(String blockMacroClassName, Object blockName);
    void block_macro(Class<BlockMacroProcessor> blockMacroClass, Object blockName);
    void block_macro(BlockMacroProcessor blockMacroInstance, Object blockName);
    void inline_macro(Object blockSymbol, String blockClassName);
    void include_processor(String includeProcessorClassName);
    void include_processor(IncludeProcessor includeProcessor);
    void treeprocessor(String treeprocessor);
    void treeprocessor(Treeprocessor treeprocessorClassName);
    void unregister_all_extensions();
    
	Object render(String content, Map<String, Object> options);
	Object render_file(String filename, Map<String, Object> options);
	
	DocumentRuby load_file(String filename, Map<String, Object> options);
	DocumentRuby load(String content, Map<String, Object> options);

	String asciidoctorRuntimeEnvironmentVersion();
	
}
