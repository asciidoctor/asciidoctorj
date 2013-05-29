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
	 * @return this instance.
	 */
	public AttributesBuilder linkCss(boolean linkCss) {
		this.attributes.setLinkCss(linkCss);
		return this;
	}
	
	/**
	 * Sets copy css attribute.
	 * @param copyCss true if css should be copied to the output location, false otherwise.
	 * @return this instance.
	 */
	public AttributesBuilder copyCss(boolean copyCss) {
		this.attributes.setCopyCss(copyCss);
		return this;
	}
	
	/**
	 * Sets icons attribute.
	 * @param icons true if icons are rendered, false otherwise.
	 * @return this instance.
	 */
	public AttributesBuilder icons(boolean icons) {
		this.attributes.setIcons(icons);
		return this;
	}
	
	/**
	 * Sets icons directory location.
	 * @param iconsDir location.
	 * @return this instance.
	 */
	public AttributesBuilder iconsDir(String iconsDir) {
		this.attributes.setIconsDir(iconsDir);
		return this;
	}
	
	/**
	 * Sets data-uri attribute.
	 * @param dataUri true if images should be embedded, false otherwise.
	 */
	public AttributesBuilder dataUri(boolean dataUri) {
		this.attributes.setDataUri(dataUri);
		return this;
	}
	
	/**
	 * Sets custom or unlisted attribute to the default value, empty string.
	 * @param attributeName A flag-only attribute, such as "icons"
	 * @return this instance.
	 */
	public AttributesBuilder attribute(String attributeName) {
		this.attributes.setAttribute(attributeName, "");
		return this;
	}

	/**
	 * Auto-number section titles in the HTML backend.
	 * @param sectionNumbers true if numbers should be autonumbered, false otherwise. 
	 * @return this instance.
	 */
	public AttributesBuilder sectionNumbers(boolean sectionNumbers) {
		this.attributes.setSectionNumbers(sectionNumbers);
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
	 * Adds all attributes.
	 * @param attributes map.
	 * @return this instance.
	 */
	public AttributesBuilder attributes(Map<String, Object> attributes) {
		this.attributes.setAttributes(attributes);
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
