package org.asciidoctor;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Attributes {

	private static Format DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static Format TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z"); 
	
	protected static final String BACKEND = Options.BACKEND;
	protected static final String TITLE = "title";
	protected static final String DOCTYPE = Options.DOCTYPE;
	protected static final String IMAGESDIR = "imagesdir";
	protected static final String SOURCE_HIGHLIGHTER = "source-highlighter";
	protected static final String LOCALDATE = "localdate";
	protected static final String LOCALTIME = "localtime";
	protected static final String DOCDATE = "docdate";
	protected static final String DOCTIME = "doctime";
	protected static final String TOC = "toc";
	protected static final String STYLESHEET_NAME = "stylesheet";
	protected static final String STYLES_DIR = "stylesdir";
	protected static final String NOT_STYLESHEET_NAME = STYLESHEET_NAME+"!";
	protected static final String LINK_CSS = "linkcss";

	private Map<String, Object> attributes = new HashMap<String, Object>();
	
	public Attributes() {
		super();
	}
	
	public Attributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public void setBackend(String backend) {
		this.attributes.put(BACKEND, backend);
	}
	
	public void setTitle(String title) {
		this.attributes.put(TITLE, title);
	}
	
	public void setDocType(String docType) {
		this.attributes.put(DOCTYPE, docType);
	}
	
	public void setImagesDir(String imagesDir) {
		this.attributes.put(IMAGESDIR, imagesDir);
	}
	
	public void setSourceHighlighter(String sourceHighlighter) {
		this.attributes.put(SOURCE_HIGHLIGHTER, sourceHighlighter);
	}
	
	/**
	 * Sets if a table of contents should be rendered or not.
	 * @param toc value.
	 */
	public void setTableOfContents(boolean toc) {
		this.attributes.put(TOC, toAsciidoctorFlag(toc));
	}
	
	/**
	 * Sets date in format yyyy-MM-dd
	 * @param localDate object.
	 */
	public void setLocalDate(Date localDate) {
		this.attributes.put(LOCALDATE, toDate(localDate));
	}
	
	/**
	 * Sets time in format HH:mm:ss z
	 * @param localTime object.
	 */
	public void setLocalTime(Date localTime) {
		this.attributes.put(LOCALTIME, toTime(localTime));
	}
	
	/**
	 * Sets date in format yyyy-MM-dd
	 * @param docDate object.
	 */
	public void setDocDate(Date docDate) {
		this.attributes.put(DOCDATE, toDate(docDate));
	}
	
	/**
	 * Sets time in format HH:mm:ss z
	 * @param docTime object.
	 */
	public void setDocTime(Date docTime) {
		this.attributes.put(DOCTIME, toTime(docTime));
	}
	
	/**
	 * Sets stylesheet name.
	 * @param styleSheetName of css file.
	 */
	public void setStyleSheetName(String styleSheetName) {
		this.attributes.put(STYLESHEET_NAME, styleSheetName);
	}
	
	/**
	 * Unsets stylesheet name so document will be generated without style.
	 */
	public void unsetStyleSheet() {
		this.attributes.put(NOT_STYLESHEET_NAME, toAsciidoctorFlag(true));
	}
	
	/**
	 * Sets the styles dir.
	 * @param stylesDir directory.
	 */
	public void setStylesDir(String stylesDir) {
		this.attributes.put(STYLES_DIR, stylesDir);
	}
	
	/**
	 * Sets link css attribute.
	 * @param linkCss true if css is linked, false if css is embedded.
	 */
	public void setLinkCss(boolean linkCss) {
		this.attributes.put(LINK_CSS, toAsciidoctorFlag(linkCss));
	}
	
	public void setAttribute(String attributeName, Object attributeValue) {
		this.attributes.put(attributeName, attributeValue);
	}
	
	public Map<String, Object> map() {
		return this.attributes;
	}

	private String toAsciidoctorFlag(boolean flag) {
		return flag ? "":null;
	}
	
	private static String toDate(Date date) {
		return DATE_FORMAT.format(date);
	}
	
	private static String toTime(Date time) {
		return TIME_FORMAT.format(time);
	}
	
}