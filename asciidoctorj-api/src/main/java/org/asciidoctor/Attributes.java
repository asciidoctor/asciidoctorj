package org.asciidoctor;

import java.net.URI;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Attributes {

    private static final char ATTRIBUTE_SEPARATOR = '=';
    private static Format DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static Format TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z");

    public static final String TOC = "toc";
    public static final String TOC_POSITION = "toc-position";
    public static final String TOC_LEVELS = "toclevels";
    public static final String TOC_2 = "toc2";
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
    public static final String NOT_STYLESHEET_NAME = STYLESHEET_NAME + "!";
    public static final String LINK_CSS = "linkcss";
    public static final String COPY_CSS = "copycss";
    public static final String ICONS = "icons";
    public static final String ICONFONT_REMOTE = "iconfont-remote";
    public static final String ICONFONT_CDN = "iconfont-cdn";
    public static final String ICONFONT_NAME = "iconfont-name";
    public static final String ICONS_DIR = "iconsdir";
    public static final String DATA_URI = "data-uri";
    public static final String SECTION_NUMBERS = "numbered";
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

    private Map<String, Object> attributes = new LinkedHashMap<>();

    /**
     * @deprecated Use {@link #builder} instead.
     */
    @Deprecated
    public Attributes() {
        super();
    }
    
    /**
     * @deprecated Use {@link #builder} instead.
     */
    @Deprecated
    public Attributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * @deprecated Use {@link #builder} instead.
     */
    @Deprecated
    public Attributes(String[] attributes) {
        this.setAttributes(attributes);
    }

    /**
     * @deprecated Use {@link #builder} instead.
     */
    @Deprecated
    public Attributes(String attributes) {
        this.setAttributes(attributes);
    }

    /**
     * @return Empty AttributesBuilder instance.
     */
    public static AttributesBuilder builder() {
        return new AttributesBuilder();
    }

    /**
     * Allow Asciidoctor to read content from an URI.
     * Additionally the option {@link SafeMode} must be less than {@link SafeMode#SECURE} to enable inclusion of content from an URI.
     * @see <a href="http://asciidoctor.org/docs/user-manual/#include-content-from-a-uri">Asciidoctor User Manual</a>
     * @param allowUriRead {@code true} to enable inclusion of content from an URI
     */
    public void setAllowUriRead(boolean allowUriRead) {
        this.attributes.put(ALLOW_URI_READ, toAsciidoctorFlag(allowUriRead));
    }

    /**
     * Define how to handle missing attribute references.
     * Possible values are:
     * <table>
     *     <tr>
     *         <td>{@code skip}</td>
     *         <td>leave the reference in place (default setting)</td>
     *     </tr>
     *     <tr>
     *         <td>{@code drop}</td>
     *         <td>drop the reference, but not the line</td>
     *     </tr>
     *     <tr>
     *         <td>{@code drop-line}</td>
     *         <td>drop the line on which the reference occurs (compliant behavior)</td>
     *     </tr>
     *     <tr>
     *         <td>{@code warn}</td>
     *         <td>print a warning about the missing attribute</td>
     *     </tr>
     * </table>
     * @see <a href="http://asciidoctor.org/docs/user-manual/#catch-a-missing-or-undefined-attribute">Asciidoctor User Manual</a>
     * @param attributeMissing One of the constants shown in the table above.
     */
    public void setAttributeMissing(String attributeMissing) {
        this.attributes.put(ATTRIBUTE_MISSING, attributeMissing);
    }

    /**
     * Define how to handle expressions that undefine an attribute.
     * Possible values are:
     * <table>
     *     <tr>
     *         <td>{@code drop}</td>
     *         <td>substitute the expression with an empty string after processing it</td>
     *     </tr>
     *     <tr>
     *         <td>{@code drop-line}</td>
     *         <td>drop the line that contains this expression (default setting and compliant behavior)</td>
     *     </tr>
     * </table>
     * @see <a href="http://asciidoctor.org/docs/user-manual/#catch-a-missing-or-undefined-attribute">Asciidoctor User Manual</a>
     * @param attributeUndefined One of the constants shown in the table above.
     */
    public void setAttributeUndefined(String attributeUndefined) {
        this.attributes.put(ATTRIBUTE_UNDEFINED, attributeUndefined);
    }

    /**
     * Sets the backend attribute.
     * @param backend The name of the backend, e.g. {@code docbook}.
     */
    public void setBackend(String backend) {
        this.attributes.put(BACKEND, backend);
    }

    public void setTitle(String title) {
        this.attributes.put(TITLE, title);
    }

    /**
     * Sets the document type, which defines how a document and what parts are rendered.
     * Possible values are:
     * <ul>
     *     <li>{@code article}</li>
     *     <li>{@code book}</li>
     *     <li>{@code inline}</li>
     *     <li>{@code manpage}</li>
     * </ul>
     * @see <a href="http://asciidoctor.org/docs/user-manual/#document-types">Asciidoctor User Manual</a>
     * @param docType One of the constants mentioned above.
     */
    public void setDocType(String docType) {
        this.attributes.put(DOCTYPE, docType);
    }

    /**
     * Sets the directory to which images are resolved if the image target is a relative path.
     * @see <a href="http://asciidoctor.org/docs/user-manual/#set-the-images-directory">Asciidoctor User Manual</a>
     * @param imagesDir The name of the directory.
     */
    public void setImagesDir(String imagesDir) {
        this.attributes.put(IMAGESDIR, imagesDir);
    }

    /**
     * Globally sets the source language attribute when rendering source blocks.
     * @see <a href="http://asciidoctor.org/docs/user-manual/#source-code-blocks">Asciidoctor User Manual</a>
     * @param sourceLanguage The default source language to use, e.g. {@code Java}.
     */
    public void setSourceLanguage(String sourceLanguage) {
        this.attributes.put(SOURCE_LANGUAGE, sourceLanguage);
    }

    /**
     * Sets the source highlighter to use for rendering source blocks.
     * Possible values are:
     * <ul>
     *     <li>coderay</li>
     *     <li>highlightjs</li>
     *     <li>prettify</li>
     * </ul>
     * @see <a href="http://asciidoctor.org/docs/user-manual/#source-code-blocks">Asciidoctor User Manual</a>
     * @param sourceHighlighter One of the constants mentioned above.
     */
    public void setSourceHighlighter(String sourceHighlighter) {
        this.attributes.put(SOURCE_HIGHLIGHTER, sourceHighlighter);
    }

    /**
     * Defines how many documents can be recursively included.
     * @see <a href="https://github.com/asciidoctor/asciidoctor/issues/581">Track include depth and enforce maximum depth #581</a>
     * @param maxIncludeDepth A positive integer.
     */
    public void setMaxIncludeDepth(int maxIncludeDepth) {
        this.attributes.put(MAX_INCLUDE_DEPTH, maxIncludeDepth);
    }

    /**
     * Sets the depth of section numbering.
     * That is if set to 1 only the top level section will be assigned a number.
     * Default is 3.
     * @param sectNumLevels A positive integer.
     */
    public void setSectNumLevels(int sectNumLevels) {
        this.attributes.put(SECT_NUM_LEVELS, sectNumLevels);
    }

    /**
     * Enables or disables preserving of line breaks in a paragraph.
     * @see <a href="http://asciidoctor.org/docs/user-manual/#line-breaks">Asciidoctor User Manual</a>
     * @param hardbreaks {@code true} to enable preserving of line breaks in paragraphs
     */
    public void setHardbreaks(boolean hardbreaks) {
        this.attributes.put(HARDBREAKS, toAsciidoctorFlag(hardbreaks));
    }

    /**
     * Enables or disables caching of content read from URIs
     * @see <a href="http://asciidoctor.org/docs/user-manual/#include-content-from-a-uri">Asciidoctor User Manual</a>
     * @param cacheUri {@code true} to enable caching of content read from URIs
     */
    public void setCacheUri(boolean cacheUri) {
        this.attributes.put(CACHE_URI, toAsciidoctorFlag(cacheUri));
    }

    /**
     * Enables or disables rendering of the URI scheme when rendering URLs.
     * @see <a href="http://asciidoctor.org/docs/user-manual/#url">Asciidoctor User Manual</a>
     * @param hideUriScheme
     */
    public void setHideUriScheme(boolean hideUriScheme) {
        this.attributes.put(HIDE_URI_SCHEME, toAsciidoctorFlag(hideUriScheme));
    }

    /**
     * Defines the prefix added to appendix sections.
     * The default value is {@code Appendix}
     * @see <a href="http://asciidoctor.org/docs/user-manual/#user-appendix">Asciidoctor User Manual</a>
     * @param appendixCaption The string that is prefixed to the section name in the appendix.
     */
    public void setAppendixCaption(String appendixCaption) {
        this.attributes.put(APPENDIX_CAPTION, appendixCaption);
    }


    /**
     * Sets the interpreter to use for rendering stems, i.e. equations and formulas.
     * @see <a href="http://asciidoctor.org/docs/user-manual/#activating-stem-support">Asciidoctor User Manual</a>
     * @param math The name of the interpreter, i.e. either {@code asciimath} or {@code latexmath}.
     */
    public void setMath(String math) {
        this.attributes.put(MATH, math);
    }

    /**
     * Skips front matter.
     * 
     * @param skipFrontMatter
     *            value.
     */
    public void setSkipFrontMatter(boolean skipFrontMatter) {
        this.attributes.put(SKIP_FRONT_MATTER, toAsciidoctorFlag(skipFrontMatter));
    }

    /**
     * Sets setanchor flag.
     * 
     * @param setAnchors
     *            value.
     */
    public void setAnchors(boolean setAnchors) {
        this.attributes.put(SET_ANCHORS, setAnchors);
    }

    /**
     * Sets the untitled label value.
     * 
     * @param untitledLabel
     *            value.
     */
    public void setUntitledLabel(String untitledLabel) {
        this.attributes.put(UNTITLED_LABEL, untitledLabel);
    }

    /**
     * Sets ignore undefined flag so lines are kept when they contain a reference to a missing attribute.
     * 
     * @param ignoreUndefinedAttributes
     *            value.
     */
    public void setIgnoreUndefinedAttributes(boolean ignoreUndefinedAttributes) {
        this.attributes.put(IGNORE_UNDEFINED, toAsciidoctorFlag(ignoreUndefinedAttributes));
    }

    /**
     * Sets table of contents 2 attribute.
     * 
     * @param placement
     *            where toc is rendered.
     * @deprecated Use {@link #setTableOfContents(Placement)}
     */
    @Deprecated
    public void setTableOfContents2(Placement placement) {
        this.attributes.put(TOC_2, toAsciidoctorFlag(true));
        this.attributes.put(TOC_POSITION, placement.getPosition());
    }

    /**
     * Sets if a table of contents should be rendered or not.
     * 
     * @param placement
     *            position of toc.
     */
    public void setTableOfContents(Placement placement) {
        this.attributes.put(TOC, placement.getPosition());
    }

    /**
     * Sets the amount of levels which should be shown in the toc.
     * 
     * @param levels
     *            number of levels which should be shown in the toc.
     */
    public void setTocLevels(int levels) {
        this.attributes.put(TOC_LEVELS, levels);
    }

    /**
     * Sets showtitle value as an alias for notitle!
     * 
     * @param showTitle
     *            value.
     */
    public void setShowTitle(boolean showTitle) {
        if (showTitle) {
            this.attributes.put(SHOW_TITLE, true);
            this.attributes.remove(NOTITLE);
        } else {
            this.attributes.put(NOTITLE, true);
            this.attributes.remove(SHOW_TITLE);
        }
    }

    /**
     * Sets if a table of contents should be rendered or not.
     * 
     * @param toc
     *            value.
     */
    public void setTableOfContents(boolean toc) {
        if (toc) {
            this.attributes.put(TOC, "");
        } else {
            this.attributes.remove(TOC);
        }
    }

    /**
     * Sets date in format yyyy-MM-dd
     * 
     * @param localDate
     *            object.
     */
    public void setLocalDate(Date localDate) {
        this.attributes.put(LOCALDATE, toDate(localDate));
    }

    /**
     * Sets time in format HH:mm:ss z
     * 
     * @param localTime
     *            object.
     */
    public void setLocalTime(Date localTime) {
        this.attributes.put(LOCALTIME, toTime(localTime));
    }

    /**
     * Sets date in format yyyy-MM-dd
     * 
     * @param docDate
     *            object.
     */
    public void setDocDate(Date docDate) {
        this.attributes.put(DOCDATE, toDate(docDate));
    }

    /**
     * Sets time in format HH:mm:ss z
     * 
     * @param docTime
     *            object.
     */
    public void setDocTime(Date docTime) {
        this.attributes.put(DOCTIME, toTime(docTime));
    }

    /**
     * Sets stylesheet name.
     * 
     * @param styleSheetName
     *            of css file.
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
     * 
     * @param stylesDir
     *            directory.
     */
    public void setStylesDir(String stylesDir) {
        this.attributes.put(STYLES_DIR, stylesDir);
    }

    /**
     * Sets link css attribute.
     * 
     * @param linkCss
     *            true if css is linked, false if css is embedded.
     */
    public void setLinkCss(boolean linkCss) {
        this.attributes.put(LINK_CSS, toAsciidoctorFlag(linkCss));
    }

    /**
     * Sets copy css attribute.
     * 
     * @param copyCss
     *            true if css should be copied to the output location, false otherwise.
     */
    public void setCopyCss(boolean copyCss) {
        this.attributes.put(COPY_CSS, toAsciidoctorFlag(copyCss));
    }

    /**
     * Sets which admonition icons to use. Attributes.IMAGE_ICONS constant can be used to use the original icons with
     * images or Attributes.FONT_ICONS for font icons (font-awesome).
     * 
     * @param iconsName
     *            value.
     */
    public void setIcons(String iconsName) {
        this.attributes.put(ICONS, iconsName);
    }

    /**
     * Enable icon font remote attribute. If enabled, will use the iconfont-cdn value to load the icon font URI; if
     * disabled, will use the iconfont-name value to locate the icon font CSS file
     * 
     * @param iconFontRemote
     *            true if attribute enabled false otherwise.
     */
    public void setIconFontRemote(boolean iconFontRemote) {
        this.attributes.put(ICONFONT_REMOTE, toAsciidoctorFlag(iconFontRemote));
    }

    /**
     * The URI prefix of the icon font; looks for minified CSS file based on iconfont-name value; used when
     * iconfont-remote is set
     * 
     * @param cdnUri
     *            uri where css is stored.
     */
    public void setIconFontCdn(URI cdnUri) {
        this.attributes.put(ICONFONT_CDN, cdnUri.toASCIIString());
    }

    /**
     * The name of the stylesheet in the stylesdir to load (.css extension added automatically)
     * 
     * @param iconFontName
     *            stylesheet name without .css extension.
     */
    public void setIconFontName(String iconFontName) {
        this.attributes.put(ICONFONT_NAME, iconFontName);
    }

    /**
     * Sets data-uri attribute.
     * 
     * @param dataUri
     *            true if images should be embedded, false otherwise.
     */
    public void setDataUri(boolean dataUri) {
        this.attributes.put(DATA_URI, toAsciidoctorFlag(dataUri));
    }

    /**
     * Sets icons directory.
     * 
     * @param iconsDir
     */
    public void setIconsDir(String iconsDir) {
        this.attributes.put(ICONS_DIR, iconsDir);
    }

    /**
     * auto-number section titles in the HTML backend
     * 
     * @param sectionNumbers
     */
    public void setSectionNumbers(boolean sectionNumbers) {
        this.attributes.put(SECTION_NUMBERS, toAsciidoctorFlag(sectionNumbers));
    }

    /**
     * Sets linkattrs attribute.
     * 
     * @param linkAttrs
     *            true if Asciidoctor should parse link macro attributes, false otherwise.
     */
    public void setLinkAttrs(boolean linkAttrs) {
        this.attributes.put(LINK_ATTRS, toAsciidoctorFlag(linkAttrs));
    }

    /**
     * Sets experimental attribute.
     * 
     * @param experimental
     *            true if experimental features should be enabled, false otherwise.
     */
    public void setExperimental(boolean experimental) {
        this.attributes.put(EXPERIMENTAL, experimental);
    }

    /**
     * Sets nofooter attribute.
     * 
     * @param noFooter
     *            true if the footer block should not be shown, false otherwise.
     */
    public void setNoFooter(boolean noFooter) {
        this.attributes.put(NO_FOOTER, toAsciidoctorFlag(noFooter));
    }

    /**
     * Sets compat-mode attribute.
     * 
     * @param compatMode
     *            value.
     */
    public void setCompatMode(CompatMode compatMode) {
        this.attributes.put(COMPAT_MODE, compatMode.getMode());
    }

    public void setAttribute(String attributeName, Object attributeValue) {
        this.attributes.put(attributeName, attributeValue);
    }

    /**
     * Sets attributes in string form. An example of a valid string would be:
     * 
     * 'toc numbered source-highlighter=coderay'
     * 
     * where you are adding three attributes: toc, numbered and source-highlighter with value coderay.
     * 
     * @param attributes
     *            in string format.
     */
    public void setAttributes(String attributes) {

        String[] allAttributes = attributes.split(" ");
        addAttributes(allAttributes);
    }

    /**
     * Sets attributes in array form. An example of a valid array would be:
     * 
     * '['toc', 'numbered']'
     * 
     * where you are adding three attributes: toc and numbered.
     * 
     * @param attributes
     *            in array format.
     */
    public void setAttributes(String... attributes) {
        addAttributes(attributes);
    }

    private void addAttributes(String[] allAttributes) {
        for (String attribute : allAttributes) {
            int equalsIndex = attribute.indexOf(ATTRIBUTE_SEPARATOR);
            if (equalsIndex > -1) {
                extractAttributeNameAndValue(attribute, equalsIndex);
            } else {
                this.attributes.put(attribute, "");
            }
        }
    }

    private void extractAttributeNameAndValue(String attribute, int equalsIndex) {
        String attributeName = attribute.substring(0, equalsIndex);
        String attributeValue = attribute.substring(equalsIndex + 1, attribute.length());

        this.attributes.put(attributeName, attributeValue);
    }

    /**
     * Adds all attributes.
     * 
     * @param attributes
     *            to add.
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
    }

    public static String toAsciidoctorFlag(boolean flag) {
        return flag ? "" : null;
    }

    private static String toDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    private static String toTime(Date time) {
        return TIME_FORMAT.format(time);
    }

    /**
     * @deprecated For internal use only.
     */
    @Deprecated
    public Map<String, Object> map() {
        return this.attributes;
    }
}
