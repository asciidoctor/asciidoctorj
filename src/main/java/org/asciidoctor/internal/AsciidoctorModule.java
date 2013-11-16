package org.asciidoctor.internal;

import java.util.Map;


public interface AsciidoctorModule {

    void preprocessor(String preprocessorClassName);
    void postprocessor(String postprocessorClassName);
    void block_processor(Object blockSymbol, String blockClassName);
    void block_macro(Object blockSymbol, String blockClassName);
    void inline_macro(Object blockSymbol, String blockClassName);
    void include_processor(String includeProcessorClassName);
    void treeprocessor(String treeprocessor);
    
	Object render(String content, Map<String, Object> options);
	Object render_file(String filename, Map<String, Object> options);
	
	DocumentRuby load_file(String filename, Map<String, Object> options);
	DocumentRuby load(String content, Map<String, Object> options);

	String asciidoctorRuntimeEnvironmentVersion();
	
}
