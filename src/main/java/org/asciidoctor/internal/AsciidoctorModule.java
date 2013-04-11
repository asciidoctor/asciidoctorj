package org.asciidoctor.internal;

import java.util.Map;

public interface AsciidoctorModule {

	String render(String content, Map<String, Object> options);
	String render_file(String filename, Map<String, Object> options);
}
