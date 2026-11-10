package org.asciidoctor.core;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;

/**
 * Builds an {@link Attributes} object.
 */
public class AttributesBuilder {
    private final Attributes attributes = new Attributes();

    AttributesBuilder() {
    }

    public AttributesBuilder allowUriRead(boolean allowUriRead) {
        attributes.setAllowUriRead(allowUriRead);
        return this;
    }

    public AttributesBuilder attributeMissing(String attributeMissing) {
        attributes.setAttributeMissing(attributeMissing);
        return this;
    }

    public AttributesBuilder attributeUndefined(String attributeUndefined) {
        attributes.setAttributeUndefined(attributeUndefined);
        return this;
    }

    public AttributesBuilder backend(String backend) {
        attributes.setBackend(backend);
        return this;
    }

    public AttributesBuilder title(String title) {
        attributes.setTitle(title);
        return this;
    }

    public AttributesBuilder docType(String docType) {
        attributes.setDocType(docType);
        return this;
    }

    public AttributesBuilder imagesDir(String imagesDir) {
        attributes.setImagesDir(imagesDir);
        return this;
    }

    public AttributesBuilder sourceLanguage(String sourceLanguage) {
        attributes.setSourceLanguage(sourceLanguage);
        return this;
    }

    public AttributesBuilder sourceHighlighter(String sourceHighlighter) {
        attributes.setSourceHighlighter(sourceHighlighter);
        return this;
    }

    public AttributesBuilder maxIncludeDepth(int maxIncludeDepth) {
        attributes.setMaxIncludeDepth(maxIncludeDepth);
        return this;
    }

    public AttributesBuilder sectNumLevels(int sectNumLevels) {
        attributes.setSectNumLevels(sectNumLevels);
        return this;
    }

    public AttributesBuilder hardbreaks(boolean hardbreaks) {
        attributes.setHardbreaks(hardbreaks);
        return this;
    }

    public AttributesBuilder cacheUri(boolean cacheUri) {
        attributes.setCacheUri(cacheUri);
        return this;
    }

    public AttributesBuilder hiddenUriScheme(boolean hideUriScheme) {
        attributes.setHideUriScheme(hideUriScheme);
        return this;
    }

    public AttributesBuilder appendixCaption(String appendixCaption) {
        attributes.setAppendixCaption(appendixCaption);
        return this;
    }

    public AttributesBuilder math(String math) {
        attributes.setMath(math);
        return this;
    }

    public AttributesBuilder skipFrontMatter(boolean skipFrontMatter) {
        attributes.setSkipFrontMatter(skipFrontMatter);
        return this;
    }

    public AttributesBuilder setAnchors(boolean anchors) {
        attributes.setAnchors(anchors);
        return this;
    }

    public AttributesBuilder untitledLabel(String untitledLabel) {
        attributes.setUntitledLabel(untitledLabel);
        return this;
    }

    public AttributesBuilder ignoreUndefinedAttributes(boolean ignoreUndefinedAttributes) {
        attributes.setIgnoreUndefinedAttributes(ignoreUndefinedAttributes);
        return this;
    }

    public AttributesBuilder tocLevels(int tocLevels) {
        attributes.setTocLevels(tocLevels);
        return this;
    }

    public AttributesBuilder styleSheetName(String styleSheetName) {
        attributes.setStyleSheetName(styleSheetName);
        return this;
    }

    public AttributesBuilder stylesDir(String stylesDir) {
        attributes.setStylesDir(stylesDir);
        return this;
    }

    public AttributesBuilder linkCss(boolean linkCss) {
        attributes.setLinkCss(linkCss);
        return this;
    }

    public AttributesBuilder copyCss(boolean copyCss) {
        attributes.setCopyCss(copyCss);
        return this;
    }

    public AttributesBuilder icons(String icons) {
        attributes.setIcons(icons);
        return this;
    }

    public AttributesBuilder iconFontRemote(boolean iconFontRemote) {
        attributes.setIconFontRemote(iconFontRemote);
        return this;
    }

    public AttributesBuilder iconFontName(String iconFontName) {
        attributes.setIconFontName(iconFontName);
        return this;
    }

    public AttributesBuilder dataUri(boolean dataUri) {
        attributes.setDataUri(dataUri);
        return this;
    }

    public AttributesBuilder iconsDir(String iconsDir) {
        attributes.setIconsDir(iconsDir);
        return this;
    }

    public AttributesBuilder sectionNumbers(boolean sectionNumbers) {
        attributes.setSectionNumbers(sectionNumbers);
        return this;
    }

    public AttributesBuilder linkAttrs(boolean linkAttrs) {
        attributes.setLinkAttrs(linkAttrs);
        return this;
    }

    public AttributesBuilder experimental(boolean experimental) {
        attributes.setExperimental(experimental);
        return this;
    }

    public AttributesBuilder noFooter(boolean noFooter) {
        attributes.setNoFooter(noFooter);
        return this;
    }

    public AttributesBuilder tableOfContents(boolean value) {
        attributes.setTableOfContents(value);
        return this;
    }

    public AttributesBuilder tableOfContents(Placement value) {
        attributes.setTableOfContents(value);
        return this;
    }

    public AttributesBuilder showTitle(boolean value) {
        attributes.setShowTitle(value);
        return this;
    }

    public AttributesBuilder iconFontCdn(URI value) {
        attributes.setIconFontCdn(value);
        return this;
    }

    public AttributesBuilder compatMode(CompatMode value) {
        attributes.setCompatMode(value);
        return this;
    }

    public AttributesBuilder localDate(Date value) {
        attributes.setLocalDate(value);
        return this;
    }

    public AttributesBuilder localDate(LocalDate value) {
        attributes.setLocalDate(value);
        return this;
    }

    public AttributesBuilder localTime(Date value) {
        attributes.setLocalTime(value);
        return this;
    }

    public AttributesBuilder localTime(LocalTime value) {
        attributes.setLocalTime(value);
        return this;
    }

    public AttributesBuilder docDate(Date value) {
        attributes.setDocDate(value);
        return this;
    }

    public AttributesBuilder docDate(LocalDate value) {
        attributes.setDocDate(value);
        return this;
    }

    public AttributesBuilder docTime(Date value) {
        attributes.setDocTime(value);
        return this;
    }

    public AttributesBuilder docTime(LocalTime value) {
        attributes.setDocTime(value);
        return this;
    }

    public AttributesBuilder arguments(String value) {
        attributes.setAttributes(value);
        return this;
    }

    public AttributesBuilder arguments(String... value) {
        attributes.setAttributes(value);
        return this;
    }

    public AttributesBuilder unsetStyleSheet() {
        attributes.unsetStyleSheet();
        return this;
    }

    public AttributesBuilder attribute(String attributeName) {
        return attribute(attributeName, "");
    }

    public AttributesBuilder attribute(String attributeName, Object attributeValue) {
        attributes.setAttribute(attributeName, attributeValue);
        return this;
    }

    public AttributesBuilder attributes(Map<String, Object> attributes) {
        this.attributes.setAttributes(attributes);
        return this;
    }

    public Attributes build() {
        return attributes;
    }
}
