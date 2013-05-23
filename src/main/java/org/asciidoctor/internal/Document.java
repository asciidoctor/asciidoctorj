package org.asciidoctor.internal;

import java.util.Map;



public interface Document {
	
	/**
	 * 
	 * @return document title.
	 */
	String doctitle();
	/**
	 * 
	 * @return the number of blocks.
	 */
	int size();
	
	/**
	 * 
	 * @return attributes defined in document
	 */
	Map<String, Object> getAttributes();
	
}
