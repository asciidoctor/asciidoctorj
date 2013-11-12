package org.asciidoctor.internal;

import java.util.List;
import java.util.Map;



public interface DocumentRuby extends AbstractBlock {
	
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
	
	List<Block> blocks();
	
}
