package org.asciidoctor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AttributesBuilder {

	private static final String BACKEND = "backend";
	private static final String TITLE = "title";
	private static final String DOCTYPE = "doctype";
	private static final String IMAGESDIR = "imagesdir";
	private static final String SOURCE_HIGHLIGHTER = "source-highlighter";
	
	private Map<String, Object> attributes = new HashMap<String, Object>();
	
	private AttributesBuilder() {
		super();
	}

	public static AttributesBuilder attributes() {
		return new AttributesBuilder();
	}
	
	public AttributesBuilder backend(String backend) {
		this.attributes.put(BACKEND, backend);
		return this;
	}
	
	public AttributesBuilder title(String title) {
		this.attributes.put(TITLE, title);
		return this;
	}
	
	public AttributesBuilder docType(String docType) {
		this.attributes.put(DOCTYPE, docType);
		return this;
	}
	
	public AttributesBuilder imagesDir(File imagesDir) {
		this.attributes.put(IMAGESDIR, imagesDir.getAbsolutePath());
		return this;
	}
	
	public AttributesBuilder sourceHighlighter(String sourcehighlighter) {
		this.attributes.put(SOURCE_HIGHLIGHTER, sourcehighlighter);
		return this;
	}
	
	public AttributesBuilder attribute(String attributeName, Object attributeValue) {
		this.attributes.put(attributeName, attributeValue);
		return this;
	}
	
	public Map<String, Object> asMap() {
		return this.attributes;
	}
	
}
