package org.asciidoctor.internal;

import java.util.Map;


public interface AsciidoctorModule {

    void preprocessor(String preprocessorClassName);
    void postprocessor(String postprocessorClassName);
    
	Object render(String content, Map<String, Object> options);
	Object render_file(String filename, Map<String, Object> options);
	
	Document load_file(String filename, Map<String, Object> options);
	Document load(String content, Map<String, Object> options);

}
