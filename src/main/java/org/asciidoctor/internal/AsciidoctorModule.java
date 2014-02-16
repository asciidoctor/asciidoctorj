package org.asciidoctor.internal;

import java.util.Map;

import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Treeprocessor;


public interface AsciidoctorModule {

    void preprocessor(String preprocessorClassName);
	void preprocessor(Preprocessor preprocessor);
    void postprocessor(String postprocessorClassName);
    void postprocessor(Postprocessor postprocessor);
    void block_processor(Object blockSymbol, String blockClassName);
    void block_processor(Object blockSymbol, BlockProcessor blockClassName);
    void block_macro(Object blockSymbol, String blockClassName);
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
