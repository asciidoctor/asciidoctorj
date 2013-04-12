package org.asciidoctor;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AttributesBuilder {

	private static Format DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static Format TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z"); 
	
	private static final String BACKEND = "backend";
	private static final String TITLE = "title";
	private static final String DOCTYPE = "doctype";
	private static final String IMAGESDIR = "imagesdir";
	private static final String SOURCE_HIGHLIGHTER = "source-highlighter";
	private static final String LOCALDATE = "localdate";
	private static final String LOCALTIME = "localtime";
	private static final String DOCDATE = "docdate";
	private static final String DOCTIME = "doctime";
	
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

	public AttributesBuilder localDate(Date date) {
		this.attributes.put(LOCALDATE, toDate(date));
		return this;
	}
	
	public AttributesBuilder localTime(Date time) {
		this.attributes.put(LOCALTIME, toTime(time));
		return this;
	}
	
	public AttributesBuilder docDate(Date date) {
		this.attributes.put(DOCDATE, date);
		return this;
	}
	
	public AttributesBuilder docTime(Date time) {
		this.attributes.put(DOCTIME, time);
		return this;
	}
	
	public AttributesBuilder attribute(String attributeName, Object attributeValue) {
		this.attributes.put(attributeName, attributeValue);
		return this;
	}
	
	public Map<String, Object> asMap() {
		return this.attributes;
	}
	
	private static String toDate(Date date) {
		return DATE_FORMAT.format(date);
	}
	
	private static String toTime(Date time) {
		return TIME_FORMAT.format(time);
	}
	
}
