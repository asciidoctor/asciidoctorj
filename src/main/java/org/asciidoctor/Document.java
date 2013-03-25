package org.asciidoctor;

import java.util.Map;


public interface Document {

	//methods
	void initialize(String[] data, Map<Object, Object> options);
	Long counter(String name, String seed);
	String author();
	String revdate();
	boolean set_attribute(String name, String value);
	boolean delete_attribute(String name);
	boolean attribute_locked(String key);
	void update_backend_attributes();
	String render(Map<Object, Object> options);
	
	//attributes
	Map<Object, Object> references();
	
	String base_dir();
	
	Long safe();
	
	Map<Object, Object> counters();
	
	Object callouts();
	
	Object header();
	
	Object parent_document();
	
}
