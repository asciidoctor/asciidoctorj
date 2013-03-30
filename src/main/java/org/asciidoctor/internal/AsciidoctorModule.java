package org.asciidoctor.internal;

import java.util.Map;

import org.asciidoctor.Document;

public interface AsciidoctorModule {

	Document load(String content, Map<Object, Object> options);
	Document load_file(String filename, Map<Object, Object> options);
	
	String render(String content, Map<Object, Object> options);
	String render_file(String filename, Map<Object, Object> options);
}
