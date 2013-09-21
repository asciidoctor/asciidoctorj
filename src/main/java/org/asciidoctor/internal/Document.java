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
	 * @return page title
	 */
	String title();
	
	/**
	 * 
	 * @return attributes defined in document
	 */
	Map<String, Object> getAttributes();
	
	/**
	 * 
	 * @return basebackend attribute value
	 */
	boolean basebackend(String backend);
	
}
