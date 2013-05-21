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

	/**
	 * Creates attributes builder.
	 * @return atributes builder.
	 */
	public static AttributesBuilder attributes() {
		return new AttributesBuilder();
	}
	
	/**
	 * Sets backend attribue.
	 * @param backend value.
	 * @return this instance.
	 */
	public AttributesBuilder backend(String backend) {
		this.attributes.put(BACKEND, backend);
		return this;
	}
	
	/**
	 * Sets title of document.
	 * @param title for document.
	 * @return this instance.
	 */
	public AttributesBuilder title(String title) {
		this.attributes.put(TITLE, title);
		return this;
	}
	
	/**
	 * Sets doc type attribute.
	 * @param docType value.
	 * @return this instance.
	 */
	public AttributesBuilder docType(String docType) {
		this.attributes.put(DOCTYPE, docType);
		return this;
	}
	
	/**
	 * Sets image directory.
	 * @param imagesDir location.
	 * @return this instance.
	 */
	public AttributesBuilder imagesDir(File imagesDir) {
		this.attributes.put(IMAGESDIR, imagesDir.getAbsolutePath());
		return this;
	}
	
	/**
	 * Sets source highlighter processor. It should be supported by asciidoctor.
	 * @param sourcehighlighter processor.
	 * @return this instance.
	 */
	public AttributesBuilder sourceHighlighter(String sourcehighlighter) {
		this.attributes.put(SOURCE_HIGHLIGHTER, sourcehighlighter);
		return this;
	}

	/**
	 * Sets local date for document.
	 * @param date 
	 * @return this instance.
	 */
	public AttributesBuilder localDate(Date date) {
		this.attributes.put(LOCALDATE, toDate(date));
		return this;
	}
	
	/**
	 * Sets local time for document.
	 * @param time
	 * @return this instance.
	 */
	public AttributesBuilder localTime(Date time) {
		this.attributes.put(LOCALTIME, toTime(time));
		return this;
	}
	
	/**
	 * Sets doc date for current document.
	 * @param date
	 * @return this instance.
	 */
	public AttributesBuilder docDate(Date date) {
		this.attributes.put(DOCDATE, date);
		return this;
	}
	
	/**
	 * Sets doc time for current document.
	 * @param time
	 * @return this instance.
	 */
	public AttributesBuilder docTime(Date time) {
		this.attributes.put(DOCTIME, time);
		return this;
	}
	
	/**
	 * Sets custom or unlisted attribute 
	 * @param attributeName 
	 * @param attributeValue
	 * @return this instance.
	 */
	public AttributesBuilder attribute(String attributeName, Object attributeValue) {
		this.attributes.put(attributeName, attributeValue);
		return this;
	}

	/**
	 * Sets custom or unlisted attribute to the default value, empty string.
	 * @param attributeName A flag-only attribute, such as "icons"
	 * @return this instance.
	 */
	public AttributesBuilder attribute(String attributeName) {
		this.attributes.put(attributeName, "");
		return this;
	}
	
	/**
	 * Gets a map with configured options.
	 * @return map with all options. By default an empty map is returned.
	 */
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
