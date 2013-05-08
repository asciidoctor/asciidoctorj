package org.asciidoctor;

import java.util.Date;
import java.util.Map;

public class AttributesBuilder {

	private Attributes attributes = new Attributes();
	
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
		this.attributes.setBackend(backend);
		return this;
	}
	
	/**
	 * Sets title of document.
	 * @param title for document.
	 * @return this instance.
	 */
	public AttributesBuilder title(String title) {
		this.attributes.setTitle(title);
		return this;
	}
	
	/**
	 * Sets doc type attribute.
	 * @param docType value.
	 * @return this instance.
	 */
	public AttributesBuilder docType(String docType) {
		this.attributes.setDocType(docType);
		return this;
	}
	
	/**
	 * Sets image directory.
	 * @param imagesDir location.
	 * @return this instance.
	 */
	public AttributesBuilder imagesDir(String imagesDir) {
		this.attributes.setImagesDir(imagesDir);
		return this;
	}
	
	/**
	 * Sets source highlighter processor. It should be supported by asciidoctor.
	 * @param sourcehighlighter processor.
	 * @return this instance.
	 */
	public AttributesBuilder sourceHighlighter(String sourceHighlighter) {
		this.attributes.setSourceHighlighter(sourceHighlighter);
		return this;
	}

	/**
	 * Sets local date for document.
	 * @param date 
	 * @return this instance.
	 */
	public AttributesBuilder localDate(Date date) {
		this.attributes.setLocalDate(date);
		return this;
	}
	
	/**
	 * Sets local time for document.
	 * @param time
	 * @return this instance.
	 */
	public AttributesBuilder localTime(Date time) {
		this.attributes.setLocalTime(time);
		return this;
	}
	
	/**
	 * Sets doc date for current document.
	 * @param date
	 * @return this instance.
	 */
	public AttributesBuilder docDate(Date date) {
		this.attributes.setDocDate(date);
		return this;
	}
	
	/**
	 * Sets doc time for current document.
	 * @param time
	 * @return this instance.
	 */
	public AttributesBuilder docTime(Date time) {
		this.attributes.setDocTime(time);
		return this;
	}
	
	/**
	 * Sets if table of contents should be rendered or not
	 * @param toc value
	 * @return this instance.
	 */
	public AttributesBuilder tableOfContents(boolean toc) {
		this.attributes.setTableOfContents(toc);
		return this;
	}
	
	/**
	 * Sets stylesheet name.
	 * @param styleSheetName of css file.
	 * @return this instance.
	 */
	public AttributesBuilder styleSheetName(String styleSheetName) {
		this.attributes.setStyleSheetName(styleSheetName);
		return this;
	}
	
	/**
	 * Unsets stylesheet name so document will be generated without style.
	 * @return this instance.
	 */
	public AttributesBuilder unsetStyleSheet() {
		this.attributes.unsetStyleSheet();
		return this;
	}
	
	/**
	 * Sets the styles dir.
	 * @param stylesDir directory.
	 * @return this instance.
	 */
	public AttributesBuilder stylesDir(String stylesDir) {
		this.attributes.setStylesDir(stylesDir);
		return this;
	}
	
	/**
	 * Sets link css attribute.
	 * @param linkCss true if css is linked, false if css is embedded.
	 * 
	 * @return this instance.
	 */
	public AttributesBuilder linkCss(boolean linkCss) {
		this.attributes.setLinkCss(linkCss);
		return this;
	}
	
	/**
	 * Sets custom or unlisted attribute 
	 * @param attributeName 
	 * @param attributeValue
	 * @return this instance.
	 */
	public AttributesBuilder attribute(String attributeName, Object attributeValue) {
		this.attributes.setAttribute(attributeName, attributeValue);
		return this;
	}
	
	/**
	 * Gets a map with configured options.
	 * @return map with all options. By default an empty map is returned.
	 */
	public Map<String, Object> asMap() {
		return this.attributes.map();
	}
	
	public Attributes get() {
		return this.attributes;
	}
	
}
