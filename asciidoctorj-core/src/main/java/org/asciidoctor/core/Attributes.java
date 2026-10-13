package org.asciidoctor.core;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The document attributes passed to the processor (using {@link Options#setAttributes(Attributes)}).
 *
 * <p>Use {@link #builder()} to create an instance. A boolean attribute is set to an empty String when enabled and to
 * {@code null} (which unsets it) when disabled.
 */
public class Attributes {
    public static final String TOC = "toc";
    public static final String TOC_LEVELS = "toclevels";
    public static final String BACKEND = Options.BACKEND;
    public static final String TITLE = "title";
    public static final String DOCTYPE = Options.DOCTYPE;
    public static final String IMAGESDIR = "imagesdir";
    public static final String SOURCE_HIGHLIGHTER = "source-highlighter";
    public static final String SOURCE_LANGUAGE = "source-language";
    public static final String LOCALDATE = "localdate";
    public static final String LOCALTIME = "localtime";
    public static final String DOCDATE = "docdate";
    public static final String DOCTIME = "doctime";
    public static final String STYLESHEET_NAME = "stylesheet";
    public static final String STYLES_DIR = "stylesdir";
    public static final String NOT_STYLESHEET_NAME = "stylesheet!";
    public static final String LINK_CSS = "linkcss";
    public static final String COPY_CSS = "copycss";
    public static final String ICONS = "icons";
    public static final String ICONFONT_REMOTE = "iconfont-remote";
    public static final String ICONFONT_CDN = "iconfont-cdn";
    public static final String ICONFONT_NAME = "iconfont-name";
    public static final String ICONS_DIR = "iconsdir";
    public static final String DATA_URI = "data-uri";
    public static final String SECTION_NUMBERS = "sectnums";
    public static final String IMAGE_ICONS = "";
    public static final String FONT_ICONS = "font";
    public static final String LINK_ATTRS = "linkattrs";
    public static final String EXPERIMENTAL = "experimental";
    public static final String SHOW_TITLE = "showtitle";
    public static final String NOTITLE = "notitle";
    public static final String ALLOW_URI_READ = "allow-uri-read";
    public static final String IGNORE_UNDEFINED = "ignore-undefined";
    public static final String UNTITLED_LABEL = "untitled-label";
    public static final String SET_ANCHORS = "sectanchors";
    public static final String SKIP_FRONT_MATTER = "skip-front-matter";
    public static final String MAX_INCLUDE_DEPTH = "max-include-depth";
    public static final String ATTRIBUTE_MISSING = "attribute-missing";
    public static final String ATTRIBUTE_UNDEFINED = "attribute-undefined";
    public static final String NO_FOOTER = "nofooter";
    public static final String HARDBREAKS = "hardbreaks";
    public static final String SECT_NUM_LEVELS = "sectnumlevels";
    public static final String CACHE_URI = "cache-uri";
    public static final String MATH = "stem";
    public static final String APPENDIX_CAPTION = "appendix-caption";
    public static final String HIDE_URI_SCHEME = "hide-uri-scheme";
    public static final String COMPAT_MODE = "compat-mode";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss z");

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    public Attributes() {
    }

    public static AttributesBuilder builder() {
        return new AttributesBuilder();
    }

    public void setAllowUriRead(boolean allowUriRead) {
        attributes.put(ALLOW_URI_READ, toAsciidoctorFlag(allowUriRead));
    }

    public void setAttributeMissing(String attributeMissing) {
        attributes.put(ATTRIBUTE_MISSING, attributeMissing);
    }

    public void setAttributeUndefined(String attributeUndefined) {
        attributes.put(ATTRIBUTE_UNDEFINED, attributeUndefined);
    }

    public void setBackend(String backend) {
        attributes.put(BACKEND, backend);
    }

    public void setTitle(String title) {
        attributes.put(TITLE, title);
    }

    public void setDocType(String docType) {
        attributes.put(DOCTYPE, docType);
    }

    public void setImagesDir(String imagesDir) {
        attributes.put(IMAGESDIR, imagesDir);
    }

    public void setSourceLanguage(String sourceLanguage) {
        attributes.put(SOURCE_LANGUAGE, sourceLanguage);
    }

    public void setSourceHighlighter(String sourceHighlighter) {
        attributes.put(SOURCE_HIGHLIGHTER, sourceHighlighter);
    }

    public void setMaxIncludeDepth(int maxIncludeDepth) {
        attributes.put(MAX_INCLUDE_DEPTH, maxIncludeDepth);
    }

    public void setSectNumLevels(int sectNumLevels) {
        attributes.put(SECT_NUM_LEVELS, sectNumLevels);
    }

    public void setHardbreaks(boolean hardbreaks) {
        attributes.put(HARDBREAKS, toAsciidoctorFlag(hardbreaks));
    }

    public void setCacheUri(boolean cacheUri) {
        attributes.put(CACHE_URI, toAsciidoctorFlag(cacheUri));
    }

    public void setHideUriScheme(boolean hideUriScheme) {
        attributes.put(HIDE_URI_SCHEME, toAsciidoctorFlag(hideUriScheme));
    }

    public void setAppendixCaption(String appendixCaption) {
        attributes.put(APPENDIX_CAPTION, appendixCaption);
    }

    public void setMath(String math) {
        attributes.put(MATH, math);
    }

    public void setSkipFrontMatter(boolean skipFrontMatter) {
        attributes.put(SKIP_FRONT_MATTER, toAsciidoctorFlag(skipFrontMatter));
    }

    public void setAnchors(boolean anchors) {
        attributes.put(SET_ANCHORS, toAsciidoctorFlag(anchors));
    }

    public void setUntitledLabel(String untitledLabel) {
        attributes.put(UNTITLED_LABEL, untitledLabel);
    }

    public void setIgnoreUndefinedAttributes(boolean ignoreUndefinedAttributes) {
        attributes.put(IGNORE_UNDEFINED, toAsciidoctorFlag(ignoreUndefinedAttributes));
    }

    public void setTocLevels(int tocLevels) {
        attributes.put(TOC_LEVELS, tocLevels);
    }

    public void setStyleSheetName(String styleSheetName) {
        attributes.put(STYLESHEET_NAME, styleSheetName);
    }

    public void setStylesDir(String stylesDir) {
        attributes.put(STYLES_DIR, stylesDir);
    }

    public void setLinkCss(boolean linkCss) {
        attributes.put(LINK_CSS, toAsciidoctorFlag(linkCss));
    }

    public void setCopyCss(boolean copyCss) {
        attributes.put(COPY_CSS, toAsciidoctorFlag(copyCss));
    }

    public void setIcons(String icons) {
        attributes.put(ICONS, icons);
    }

    public void setIconFontRemote(boolean iconFontRemote) {
        attributes.put(ICONFONT_REMOTE, toAsciidoctorFlag(iconFontRemote));
    }

    public void setIconFontName(String iconFontName) {
        attributes.put(ICONFONT_NAME, iconFontName);
    }

    public void setDataUri(boolean dataUri) {
        attributes.put(DATA_URI, toAsciidoctorFlag(dataUri));
    }

    public void setIconsDir(String iconsDir) {
        attributes.put(ICONS_DIR, iconsDir);
    }

    public void setSectionNumbers(boolean sectionNumbers) {
        attributes.put(SECTION_NUMBERS, toAsciidoctorFlag(sectionNumbers));
    }

    public void setLinkAttrs(boolean linkAttrs) {
        attributes.put(LINK_ATTRS, toAsciidoctorFlag(linkAttrs));
    }

    public void setExperimental(boolean experimental) {
        attributes.put(EXPERIMENTAL, toAsciidoctorFlag(experimental));
    }

    public void setNoFooter(boolean noFooter) {
        attributes.put(NO_FOOTER, toAsciidoctorFlag(noFooter));
    }

    public void setTableOfContents(boolean toc) {
        if (toc) {
            attributes.put(TOC, "");
        } else {
            attributes.remove(TOC);
        }
    }

    public void setTableOfContents(Placement placement) {
        attributes.put(TOC, placement.getPosition());
    }

    public void setShowTitle(boolean showTitle) {
        if (showTitle) {
            attributes.put(SHOW_TITLE, "");
            attributes.remove(NOTITLE);
        } else {
            attributes.put(NOTITLE, "");
            attributes.remove(SHOW_TITLE);
        }
    }

    public void setIconFontCdn(URI cdnUri) {
        attributes.put(ICONFONT_CDN, cdnUri.toASCIIString());
    }

    public void unsetStyleSheet() {
        attributes.put(NOT_STYLESHEET_NAME, "");
    }

    public void setCompatMode(CompatMode compatMode) {
        attributes.put(COMPAT_MODE, compatMode.getMode());
    }

    public void setLocalDate(Date localDate) {
        attributes.put(LOCALDATE, formatDate(localDate));
    }

    public void setLocalDate(LocalDate localDate) {
        attributes.put(LOCALDATE, localDate.format(DATE_FORMAT));
    }

    public void setLocalTime(Date localTime) {
        attributes.put(LOCALTIME, formatTime(localTime));
    }

    public void setLocalTime(LocalTime localTime) {
        attributes.put(LOCALTIME, localTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).format(TIME_FORMAT));
    }

    public void setDocDate(Date docDate) {
        attributes.put(DOCDATE, formatDate(docDate));
    }

    public void setDocDate(LocalDate docDate) {
        attributes.put(DOCDATE, docDate.format(DATE_FORMAT));
    }

    public void setDocTime(Date docTime) {
        attributes.put(DOCTIME, formatTime(docTime));
    }

    public void setDocTime(LocalTime docTime) {
        attributes.put(DOCTIME, docTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).format(TIME_FORMAT));
    }

    public void setAttribute(String attributeName, Object attributeValue) {
        attributes.put(attributeName, attributeValue);
    }

    /**
     * Sets the attributes specified as a String of space-separated name=value pairs (e.g., "icons=font toc").
     */
    public void setAttributes(String attributes) {
        setAttributes(attributes.split(" "));
    }

    /**
     * Sets the attributes specified as name=value pairs (or names, in which case the value is an empty String).
     */
    public void setAttributes(String... attributes) {
        for (String attribute : attributes) {
            int equalsIndex = attribute.indexOf('=');
            if (equalsIndex > -1) {
                this.attributes.put(attribute.substring(0, equalsIndex), attribute.substring(equalsIndex + 1));
            } else {
                this.attributes.put(attribute, "");
            }
        }
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
    }

    /**
     * @return the value of a boolean attribute (an empty String to set the attribute, {@code null} to unset it)
     */
    public static String toAsciidoctorFlag(boolean flag) {
        return flag ? "" : null;
    }

    private static String formatDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).format(DATE_FORMAT);
    }

    private static String formatTime(Date time) {
        return ZonedDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()).format(TIME_FORMAT);
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    /**
     * @return the attributes as a (mutable) Map
     */
    public Map<String, Object> map() {
        return attributes;
    }
}
