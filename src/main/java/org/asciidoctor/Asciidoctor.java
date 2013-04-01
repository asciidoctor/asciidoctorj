package org.asciidoctor;

import java.util.Map;

import org.asciidoctor.internal.JRubyAsciidoctor;


public interface Asciidoctor {

	Document load(String content, Map<Object, Object> options);
	Document loadFile(String filename, Map<Object, Object> options);
	
	String render(String content, Map<Object, Object> options);
	String renderFile(String filename, Map<Object, Object> options);
	
	public static class Factory {
		
		public static Asciidoctor create() {
			return JRubyAsciidoctor.create();
		}
	}
	
}
