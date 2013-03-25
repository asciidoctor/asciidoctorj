package org.asciidoctor.internal;

import java.util.Map;

public interface AsciidoctorModule {

	Object load(String content, Map<Object, Object> options);
	Object load_file(String filename, Map<Object, Object> options);
	
	String render(String content, Map<Object, Object> options);
	String render_file(String filename, Map<Object, Object> options);
}
