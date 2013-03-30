package org.asciidoctor;

import java.util.Map;


public interface Document {
	String render(Map<Object, Object> options);
}
