package org.asciidoctor.core;

import static org.asciidoctor.core.Asciidoctor.BACKEND_ALIASES;
import static org.asciidoctor.core.Asciidoctor.DEFAULT_ATTRIBUTES;
import static org.asciidoctor.core.Asciidoctor.DEFAULT_BACKEND;
import static org.asciidoctor.core.Asciidoctor.DEFAULT_DOCTYPE;
import static org.asciidoctor.core.Asciidoctor.DEFAULT_PAGE_WIDTHS;
import static org.asciidoctor.core.Asciidoctor.FLEXIBLE_ATTRIBUTES;
import static org.asciidoctor.core.Asciidoctor.LF;
import static org.asciidoctor.core.Asciidoctor.USER_HOME;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import org.asciidoctor.core.Reader.Cursor;
import org.asciidoctor.core.converter.BackendTraits;
import org.asciidoctor.core.converter.Converter;
import org.asciidoctor.core.converter.ConverterFactory;
import org.asciidoctor.core.converter.ManPageConverter;
import org.asciidoctor.core.extension.DocinfoProcessor;
import org.asciidoctor.core.extension.Extensions;
import org.asciidoctor.core.extension.Group;
import org.asciidoctor.core.extension.Postprocessor;
import org.asciidoctor.core.extension.Preprocessor;
import org.asciidoctor.core.extension.ProcessorExtension;
import org.asciidoctor.core.extension.Registry;
import org.asciidoctor.core.extension.TreeProcessor;
import org.asciidoctor.core.syntaxhighlighter.SyntaxHighlighter;
import org.asciidoctor.core.syntaxhighlighter.SyntaxHighlighterFactory;

/**
 * The Document class represents a parsed AsciiDoc document.
 *
 * <p>Document is the root node of a parsed AsciiDoc document. It provides an abstract syntax tree (AST) that
 * represents the structure of the AsciiDoc document from which the Document object was parsed.
 *
 * <p>Although the constructor can be used to create an empty document object, more commonly, you'll load the
 * document object from AsciiDoc source using the primary API methods, {@link Asciidoctor#load(Object, Map)} or
 * {@link Asciidoctor#loadFile(String, Map)}. When using one of these APIs, you almost always want to set the safe mode
 * to safe (or unsafe) to enable all of Asciidoctor's features.
 *
 * <pre>
 *   Asciidoctor.load("= Hello, AsciiDoc!", Map.of("safe", "safe"));
 *   // => Document { doctype: "article", doctitle: "Hello, AsciiDoc!", blocks: 0 }
 * </pre>
 *
 * <p>Instances of this class can be used to extract information from the document or alter its structure. As such,
 * the Document object is most often used in extensions and by integrations.
 *
 * <p>The most basic usage of the Document object is to retrieve the document's title.
 *
 * <pre>
 *   document = Asciidoctor.load("= Document Title", Map.of("safe", "safe"));
 *   document.getDoctitle();
 *   // => "Document Title"
 * </pre>
 *
 * <p>If the document has no title, the {@link #getDoctitle()} method returns the title of the first section. If that
 * check falls through, you can have the method return a fallback value (the value of the untitled-label attribute).
 *
 * <p>Loading a document object is the first step in the conversion process. You can take the process to completion by
 * calling the {@link #convert()} method.
 */
public class Document extends AbstractBlock {
    /** The key under which attribute entries are stored in the attributes of a block. */
    public static final String ATTRIBUTE_ENTRIES_KEY = ":attribute_entries";

    /**
     * A reference to an image found in the document.
     *
     * @param target the target of the image
     * @param imagesdir the value of the imagesdir attribute when the image was referenced
     */
    public record ImageReference(String target, String imagesdir) {
        public String getTarget() {
            return target;
        }

        public String getImagesdir() {
            return imagesdir;
        }

        @Override
        public String toString() {
            return target;
        }
    }

    /**
     * A footnote found in the document.
     *
     * @param index the index of the footnote (null if the footnote is not numbered)
     * @param id the ID of the footnote, if any
     * @param text the text of the footnote
     */
    public record Footnote(Integer index, String id, String text) {
        public Integer getIndex() {
            return index;
        }

        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }
    }

    /** An attribute entry found in the document (e.g., :name: value). */
    public static class AttributeEntry {
        private final String name;
        private final Object value;
        private final boolean negate;

        public AttributeEntry(String name, Object value, Boolean negate) {
            this.name = name;
            this.value = value;
            this.negate = negate == null ? value == null : negate;
        }

        public AttributeEntry(String name, Object value) {
            this(name, value, null);
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public boolean isNegate() {
            return negate;
        }

        @SuppressWarnings("unchecked")
        public AttributeEntry saveTo(Map<String, Object> blockAttributes) {
            ((List<AttributeEntry>) blockAttributes.computeIfAbsent(ATTRIBUTE_ENTRIES_KEY, k -> new ArrayList<>()))
                    .add(this);
            return this;
        }
    }

    /** Parsed and stores a partitioned title (i.e., title &amp; subtitle). */
    public static class Title {
        private final String main;
        private final String subtitle;
        private final String combined;
        private final boolean sanitized;

        public Title(String val) {
            this(val, false, null);
        }

        public Title(String val, boolean sanitize, String separator) {
            // TODO separate sanitization by type (:cdata for HTML/XML, :plain_text for non-SGML, false for none)
            sanitized = sanitize;
            if (sanitize && val.contains("<")) {
                val = Strings.strip(Strings.squeeze(Rx.gsub(Rx.XML_SANITIZE_RX, val, ""), ' '));
            }
            String sep = separator == null ? ":" : separator;
            if (sep.isEmpty() || !val.contains(sep = sep + " ")) {
                main = val;
                subtitle = null;
            } else {
                String[] parts = Strings.rpartition(val, sep);
                main = parts[0];
                subtitle = parts[2];
            }
            combined = val;
        }

        public String getMain() {
            return main;
        }

        public String getTitle() {
            return main;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getCombined() {
            return combined;
        }

        public boolean isSanitized() {
            return sanitized;
        }

        public boolean hasSubtitle() {
            return subtitle != null;
        }

        @Override
        public String toString() {
            return combined;
        }
    }

    /**
     * The revision information of a document.
     *
     * @param date the revision date (the revdate attribute)
     * @param number the revision number (the revnumber attribute)
     * @param remark the revision remark (the revremark attribute)
     */
    public record RevisionInfo(String date, String number, String remark) {
        public String getDate() {
            return date;
        }

        public String getNumber() {
            return number;
        }

        public String getRemark() {
            return remark;
        }
    }

    /** Represents information about an author extracted from document attributes. */
    public record Author(String fullName, String firstName, String middleName, String lastName, String initials,
            String email) {
        public String getFullName() {
            return fullName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getInitials() {
            return initials;
        }

        public String getEmail() {
            return email;
        }
    }

    /** The document catalog, which holds the references, footnotes, links, images, callouts and includes. */
    public static class Catalog {
        private final Map<String, AbstractNode> ids;
        private final Map<String, AbstractNode> refs;
        private final List<Footnote> footnotes;
        private final List<String> links;
        private final List<ImageReference> images;
        private final Callouts callouts;
        private final Map<String, Boolean> includes;

        public Catalog() {
            this(new LinkedHashMap<>(), new LinkedHashMap<>(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new Callouts(), new LinkedHashMap<>());
        }

        private Catalog(Map<String, AbstractNode> ids, Map<String, AbstractNode> refs, List<Footnote> footnotes,
                List<String> links, List<ImageReference> images, Callouts callouts, Map<String, Boolean> includes) {
            this.ids = ids;
            this.refs = refs;
            this.footnotes = footnotes;
            this.links = links;
            this.images = images;
            this.callouts = callouts;
            this.includes = includes;
        }

        /**
         * @return a copy of this catalog that shares all the entries except for the footnotes
         */
        Catalog withNewFootnotes() {
            return new Catalog(ids, refs, new ArrayList<>(), links, images, callouts, includes);
        }

        /** @deprecated kept for backwards compatibility with converters */
        @Deprecated
        public Map<String, AbstractNode> getIds() {
            return ids;
        }

        public Map<String, AbstractNode> getRefs() {
            return refs;
        }

        public List<Footnote> getFootnotes() {
            return footnotes;
        }

        public List<String> getLinks() {
            return links;
        }

        public List<ImageReference> getImages() {
            return images;
        }

        public Callouts getCallouts() {
            return callouts;
        }

        public Map<String, Boolean> getIncludes() {
            return includes;
        }
    }

    private int safe;
    private boolean compatMode;
    private String backend;
    private String doctype;
    private boolean sourcemap;
    private final Catalog catalog;
    private final Map<String, Object> counters = new LinkedHashMap<>();
    private Section header;
    private String baseDir;
    private final Map<String, Object> options;
    private String outfilesuffix;
    private final Document parentDocument;
    private Reader reader;
    private final PathResolver pathResolver;
    private Converter<?> converter;
    private SyntaxHighlighter syntaxHighlighter;
    private Registry extensions;
    private final Map<String, Object> attributeOverrides;
    private final Set<String> attributesModified = new HashSet<>();
    private final Map<String, Object> docinfoProcessorExtensions = new LinkedHashMap<>();
    private Map<String, Object> headerAttributes;
    private Map<String, String> reftexts;
    private boolean parsed;
    private Integer maxAttributeValueSize;
    private final Timings timings;

    /**
     * The logger specified using the logger API option (or inherited from the parent document), bound to the current
     * scope when this document is parsed or converted; null to use the logger managed by {@link LoggerManager}.
     */
    private final Logger scopedLogger;

    /**
     * Initialize a Document object.
     *
     * @param data The AsciiDoc source data as a String or String List. (default: null)
     * @param options A Map of options to control processing (e.g., safe mode value (safe), backend (backend),
     *            standalone enclosure (standalone), custom attributes (attributes)). (default: {})
     */
    @SuppressWarnings("unchecked")
    public Document(Object data, Map<String, Object> options) {
        super(null, "document");
        options = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);

        Document parentDoc = (Document) options.remove("parent");
        if (options.containsKey("logger")) {
            scopedLogger = LoggerManager.resolveLoggerOption(options.remove("logger"));
        } else {
            scopedLogger = parentDoc != null ? parentDoc.scopedLogger : null;
        }
        String parentDoctype = null;
        boolean initializeExtensions;
        Map<String, Object> attrOverrides;
        Object inputMtime = null;
        if (parentDoc != null) {
            parentDocument = parentDoc;
            options.putIfAbsent("base_dir", parentDoc.getBaseDir());
            if (options.get("base_dir") == null) {
                options.put("base_dir", parentDoc.getBaseDir());
            }
            if (parentDoc.getOptions().get("catalog_assets") != null) {
                options.put("catalog_assets", true);
            }
            if (parentDoc.getOptions().get("to_dir") != null) {
                options.put("to_dir", parentDoc.getOptions().get("to_dir"));
            }
            catalog = parentDoc.getCatalog().withNewFootnotes();
            // QUESTION should we support setting attribute in parent document from nested document?
            attributeOverrides = attrOverrides = new LinkedHashMap<>(parentDoc.attributeOverrides);
            attrOverrides.putAll(parentDoc.getAttributes());
            attrOverrides.remove("compat-mode");
            parentDoctype = Strings.valueOf(attrOverrides.remove("doctype"));
            attrOverrides.remove("notitle");
            attrOverrides.remove("showtitle");
            // QUESTION if toc is hard unset in parent document, should it be hard unset in nested document?
            attrOverrides.remove("toc");
            Object tocPlacement = attrOverrides.remove("toc-placement");
            attributes.put("toc-placement", tocPlacement == null ? "auto" : tocPlacement);
            attrOverrides.remove("toc-position");
            safe = parentDoc.getSafe();
            if (compatMode = parentDoc.isCompatMode()) {
                attributes.put("compat-mode", "");
            }
            outfilesuffix = parentDoc.getOutfilesuffix();
            sourcemap = parentDoc.isSourcemap();
            timings = null;
            pathResolver = parentDoc.getPathResolver();
            converter = parentDoc.getConverter();
            initializeExtensions = false;
            extensions = parentDoc.getExtensions();
            syntaxHighlighter = parentDoc.getSyntaxHighlighter();
        } else {
            parentDocument = null;
            catalog = new Catalog();
            // copy attributes map and normalize keys
            // attribute overrides are attributes that can only be set from the commandline
            // a direct assignment effectively makes the attribute a constant
            // a null value or name with leading or trailing ! will result in the attribute being unassigned
            attributeOverrides = attrOverrides = new LinkedHashMap<>();
            Object attributesOpt = options.get("attributes");
            Map<String, Object> attributesOptMap = attributesOpt instanceof Map<?, ?>
                    ? (Map<String, Object>) attributesOpt : Options.resolveAttributes(attributesOpt);
            for (Map.Entry<String, Object> entry : attributesOptMap.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                if (key.endsWith("@")) {
                    if (key.startsWith("!")) {
                        key = key.substring(1, key.length() - 1);
                        val = false;
                    } else if (key.endsWith("!@")) {
                        key = key.substring(0, key.length() - 2);
                        val = false;
                    } else {
                        key = Strings.chop(key);
                        val = (val == null ? "" : val) + "@";
                    }
                } else if (key.startsWith("!")) {
                    key = key.substring(1);
                    val = "@".equals(val) ? false : null;
                } else if (key.endsWith("!")) {
                    key = Strings.chop(key);
                    val = "@".equals(val) ? false : null;
                }
                attrOverrides.put(key.toLowerCase(), val);
            }
            if (options.get("to_file") instanceof String toFile) {
                attrOverrides.put("outfilesuffix", Helpers.extname(toFile));
            }
            // safely resolve the safe mode from const, int or string
            Object safeMode = options.get("safe");
            if (safeMode == null) {
                safe = SafeMode.SECURE.getLevel();
            } else if (safeMode instanceof Number number) {
                // be permissive in case API user wants to define new levels
                safe = number.intValue();
            } else if (safeMode instanceof SafeMode mode) {
                safe = mode.getLevel();
            } else {
                Integer level = SafeMode.valueForName(safeMode.toString());
                safe = level == null ? SafeMode.SECURE.getLevel() : level;
            }
            inputMtime = options.remove("input_mtime");
            compatMode = attrOverrides.containsKey("compat-mode");
            // NOTE like in Ruby, any truthy value enables the sourcemap
            Object sourcemapOption = options.get("sourcemap");
            sourcemap = sourcemapOption != null && !Boolean.FALSE.equals(sourcemapOption);
            timings = (Timings) options.remove("timings");
            pathResolver = new PathResolver();
            initializeExtensions = true;
            extensions = null; // initialize further down if initialize_extensions is true
            if (options.containsKey("header_footer") && !options.containsKey("standalone")) {
                options.put("standalone", options.get("header_footer"));
            }
        }

        parsed = false;
        reftexts = null;
        header = null;
        headerAttributes = null;
        boolean standalone = Boolean.TRUE.equals(options.get("standalone"));
        this.options = Collections.unmodifiableMap(options);

        Map<String, Object> attrs = attributes;
        if (parentDoc == null) {
            attrs.put("attribute-undefined", Compliance.getAttributeUndefined());
            attrs.put("attribute-missing", Compliance.getAttributeMissing());
            attrs.putAll(DEFAULT_ATTRIBUTES);
            // TODO if lang attribute is set, @safe mode < SafeMode::SERVER, and !parent_doc,
            // load attributes from data/locale/attributes-<lang>.adoc
        }

        if (standalone) {
            // sync embedded attribute with :standalone option value
            attrOverrides.put("embedded", null);
            attrs.put("copycss", "");
            attrs.put("iconfont-remote", "");
            attrs.put("stylesheet", "");
            attrs.put("webfonts", "");
        } else {
            // sync embedded attribute with :standalone option value
            attrOverrides.put("embedded", "");
            if (attrOverrides.containsKey("showtitle") && "showtitle".equals(lastOf(attrOverrides, "notitle",
                    "showtitle"))) {
                attrOverrides.put("notitle", invertTitleOverride(attrOverrides.get("showtitle")));
            } else if (attrOverrides.containsKey("notitle")) {
                attrOverrides.put("showtitle", invertTitleOverride(attrOverrides.get("notitle")));
            } else {
                attrs.put("notitle", "");
            }
        }

        attrOverrides.put("asciidoctor", "");
        attrOverrides.put("asciidoctor-version", Asciidoctor.VERSION);

        String safeModeName = SafeMode.nameForValue(safe);
        attrOverrides.put("safe-mode-name", safeModeName);
        attrOverrides.put("safe-mode-" + safeModeName, "");
        attrOverrides.put("safe-mode-level", safe);

        // the only way to set the max-include-depth attribute is via the API; default to 64 like AsciiDoc.py
        if (attrOverrides.get("max-include-depth") == null) {
            attrOverrides.put("max-include-depth", 64);
        }

        // the only way to set the allow-uri-read attribute is via the API; disabled by default
        if (attrOverrides.get("allow-uri-read") == null) {
            attrOverrides.put("allow-uri-read", null);
        }

        // remap legacy attribute names
        if (attrOverrides.containsKey("numbered")) {
            attrOverrides.put("sectnums", attrOverrides.remove("numbered"));
        }
        if (attrOverrides.containsKey("hardbreaks")) {
            attrOverrides.put("hardbreaks-option", attrOverrides.remove("hardbreaks"));
        }

        // If the base_dir option is specified, it overrides docdir and is used as the root for relative
        // paths. Otherwise, the base_dir is the directory of the source file (docdir), if set, otherwise
        // the current directory.
        Object baseDirVal = options.get("base_dir");
        if (baseDirVal != null) {
            baseDir = Helpers.expandPath(baseDirVal.toString(), null);
            attrOverrides.put("docdir", baseDir);
        } else if (attrOverrides.get("docdir") != null) {
            baseDir = attrOverrides.get("docdir").toString();
        } else {
            //logger.warn 'setting base_dir is recommended when working with string documents' unless nested?
            baseDir = Helpers.workingDirectory();
            attrOverrides.put("docdir", baseDir);
        }

        // allow common attributes backend and doctype to be set using options hash, coerce values to string
        Object backendVal = options.get("backend");
        if (backendVal != null) {
            attrOverrides.put("backend", backendVal.toString());
        }

        Object doctypeVal = options.get("doctype");
        if (doctypeVal != null) {
            attrOverrides.put("doctype", doctypeVal.toString());
        }

        if (safe >= SafeMode.SERVER.getLevel()) {
            // restrict document from setting copycss, source-highlighter and backend
            putIfNull(attrOverrides, "copycss", null);
            putIfNull(attrOverrides, "source-highlighter", null);
            putIfNull(attrOverrides, "backend", DEFAULT_BACKEND);
            // restrict document from seeing the docdir and trim docfile to relative path
            if (parentDoc == null && attrOverrides.containsKey("docfile")) {
                String docfile = String.valueOf(attrOverrides.get("docfile"));
                String docdir = String.valueOf(attrOverrides.get("docdir"));
                attrOverrides.put("docfile", docfile.length() > docdir.length()
                        ? docfile.substring(docdir.length() + 1) : "");
            }
            attrOverrides.put("docdir", "");
            putIfNull(attrOverrides, "user-home", ".");
            if (safe >= SafeMode.SECURE.getLevel()) {
                if (!attrOverrides.containsKey("max-attribute-value-size")) {
                    attrOverrides.put("max-attribute-value-size", 4096);
                }
                // assign linkcss (preventing css embedding) unless explicitly disabled from the commandline or API
                if (!attrOverrides.containsKey("linkcss")) {
                    attrOverrides.put("linkcss", "");
                }
                // restrict document from enabling icons
                putIfNull(attrOverrides, "icons", null);
            }
        } else {
            putIfNull(attrOverrides, "user-home", USER_HOME);
        }

        // the only way to set the max-attribute-value-size attribute is via the API; disabled by default
        putIfNull(attrOverrides, "max-attribute-value-size", null);
        Object size = attrOverrides.get("max-attribute-value-size");
        maxAttributeValueSize = size != null ? Math.abs(Strings.toInt(size)) : null;

        for (Iterator<Map.Entry<String, Object>> it = attrOverrides.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            Object val = entry.getValue();
            boolean verdict = false;
            if (val != null && !Boolean.FALSE.equals(val)) {
                // a value ending in @ allows document to override value
                if (val instanceof String str && str.endsWith("@")) {
                    val = Strings.chop(str);
                    verdict = true;
                }
                attrs.put(key, val);
            } else {
                // a null or false value both unset the attribute; only a null value locks it
                attrs.remove(key);
                verdict = Boolean.FALSE.equals(val);
            }
            if (verdict) {
                it.remove();
            }
        }

        if (parentDoc != null) {
            backend = Strings.valueOf(attrs.get("backend"));
            // reset doctype unless it matches the default value
            attrs.put("doctype", parentDoctype);
            doctype = parentDoctype;
            if (!DEFAULT_DOCTYPE.equals(parentDoctype)) {
                updateDoctypeAttributes(DEFAULT_DOCTYPE);
            }

            // don't need to do the extra processing within our own document
            // FIXME line info isn't reported correctly within include files in nested document
            reader = new Reader(data, options.get("cursor"));
            if (sourcemap) {
                sourceLocation = reader.cursor();
            }

            // Now parse the lines in the reader into blocks
            // Eagerly parse (for now) since a subdocument is not a publicly accessible object
            Parser.parse(reader, this, false);

            // should we call some sort of post-parse function?
            restoreAttributes();
            parsed = true;
        } else {
            // setup default backend and doctype
            backend = null;
            Object initialBackendVal = attrs.get("backend");
            String initialBackend = initialBackendVal == null ? DEFAULT_BACKEND : initialBackendVal.toString();
            if (initialBackend.equals("manpage")) {
                doctype = "manpage";
                attrs.put("doctype", doctype);
                attrOverrides.put("doctype", doctype);
            } else {
                if (attrs.get("doctype") == null) {
                    attrs.put("doctype", DEFAULT_DOCTYPE);
                }
                doctype = attrs.get("doctype").toString();
            }
            updateBackendAttributes(initialBackend, true);

            // dynamic intrinstic attribute values

            //attrs['indir'] = attrs['docdir']
            //attrs['infile'] = attrs['docfile']

            // fallback directories
            if (attrs.get("stylesdir") == null) {
                attrs.put("stylesdir", ".");
            }
            if (attrs.get("iconsdir") == null) {
                attrs.put("iconsdir", (attrs.containsKey("imagesdir") ? attrs.get("imagesdir") : "./images")
                        + "/icons");
            }

            fillDatetimeAttributes(attrs, inputMtime);

            if (initializeExtensions) {
                Object extRegistry = options.get("extension_registry");
                if (extRegistry != null) {
                    // QUESTION should we warn if the value type of this option is not a registry
                    if (extRegistry instanceof Registry registry) {
                        extensions = registry.activate(this);
                    }
                } else {
                    Object extBlock = options.get("extensions");
                    if (extBlock == null) {
                        if (!Extensions.getGroups().isEmpty()) {
                            extensions = new Registry().activate(this);
                        }
                    } else if (extBlock instanceof Group group) {
                        extensions = Extensions.create(group).activate(this);
                    }
                }
            }

            Object docfile = attrs.get("docfile");
            reader = new PreprocessorReader(this, data, new Cursor(Strings.valueOf(docfile), baseDir), true);
            if (sourcemap) {
                sourceLocation = reader.cursor();
            }
        }
    }

    public Document(Object data) {
        this(data, null);
    }

    public Document() {
        this(null, null);
    }

    private static void putIfNull(Map<String, Object> map, String key, Object value) {
        if (map.get(key) == null) {
            map.put(key, value);
        }
    }

    private static String lastOf(Map<String, Object> map, String... keys) {
        String last = null;
        for (String key : map.keySet()) {
            for (String candidate : keys) {
                if (candidate.equals(key)) {
                    last = key;
                }
            }
        }
        return last;
    }

    private static Object invertTitleOverride(Object value) {
        if (value == null) {
            return "";
        } else if (Boolean.FALSE.equals(value)) {
            return "@";
        } else if ("@".equals(value)) {
            return false;
        }
        return null;
    }

    /**
     * Parse the AsciiDoc source stored in the {@link Reader} into an abstract syntax tree.
     *
     * <p>If the data parameter is not null, create a new {@link PreprocessorReader} and assigned it to the reader
     * property of this object. Otherwise, continue with the reader that was created in the constructor. Pass the
     * reader to {@link Parser#parse} to parse the source data into an abstract syntax tree.
     *
     * <p>If parsing has already been performed, this method returns without performing any processing.
     *
     * @param data The optional replacement AsciiDoc source data as a String or String List. (default: null)
     * @return this Document
     */
    public Document parse(Object data) {
        return withScopedLogger(() -> doParse(data));
    }

    private Document doParse(Object data) {
        if (parsed) {
            return this;
        }
        Document doc = this;
        // create reader if data is provided (used when data is not known at the time the Document object is created)
        if (data != null) {
            reader = new PreprocessorReader(doc, data,
                    new Cursor(Strings.valueOf(attributes.get("docfile")), baseDir), true);
            if (sourcemap) {
                sourceLocation = reader.cursor();
            }
        }

        Registry exts = parentDocument != null ? null : extensions;
        if (exts != null && exts.hasPreprocessors()) {
            for (ProcessorExtension<Preprocessor> ext : exts.getPreprocessors()) {
                Reader result = ext.getInstance().process(doc, (PreprocessorReader) reader);
                if (result != null) {
                    reader = result;
                }
            }
        }

        // Now parse the lines in the reader into blocks
        Parser.parse(reader, doc, Boolean.TRUE.equals(options.get("parse_header_only")));

        // should we call sort of post-parse function?
        restoreAttributes();

        if (exts != null && exts.hasTreeProcessors()) {
            for (ProcessorExtension<TreeProcessor> ext : exts.getTreeProcessors()) {
                Document result = ext.getInstance().process(doc);
                if (result != null && result != doc) {
                    doc = result;
                }
            }
        }

        parsed = true;
        return doc;
    }

    public Document parse() {
        return parse(null);
    }

    /**
     * @return whether the source lines of the document have been parsed.
     */
    public boolean isParsed() {
        return parsed;
    }

    /**
     * Get the named counter and take the next number in the sequence.
     *
     * @param name the String name of the counter
     * @param seed the initial value as a String or Integer
     * @return the next number in the sequence for the specified counter
     */
    public Object counter(String name, Object seed) {
        if (parentDocument != null) {
            return parentDocument.counter(name, seed);
        }
        boolean locked = isAttributeLocked(name);
        Object currVal;
        Object nextVal;
        if ((locked && (currVal = counters.get(name)) != null)
                || !Strings.isNullOrEmpty(currVal = attributes.get(name))) {
            nextVal = Helpers.nextval(currVal);
        } else if (seed != null) {
            String seedStr = seed.toString();
            nextVal = seed instanceof Number number ? number.intValue()
                    : seedStr.equals(String.valueOf(Strings.toLong(seedStr))) ? (int) Strings.toLong(seedStr) : seedStr;
        } else {
            nextVal = 1;
        }
        counters.put(name, nextVal);
        if (!locked) {
            attributes.put(name, nextVal);
        }
        return nextVal;
    }

    /**
     * Get the next value in the sequence of the specified counter (for compatibility with AsciidoctorJ).
     *
     * @see #counter(String, Object)
     */
    public int getAndIncrementCounter(String name, int initialValue) {
        return Strings.toInt(counter(name, initialValue));
    }

    public int getAndIncrementCounter(String name) {
        return Strings.toInt(counter(name));
    }

    public Object counter(String name) {
        return counter(name, null);
    }

    /**
     * Increment the specified counter and store it in the block's attributes.
     *
     * @param counterName the String name of the counter attribute
     * @param block the Block on which to save the counter
     * @return the next number in the sequence for the specified counter
     */
    public Object incrementAndStoreCounter(String counterName, AbstractBlock block) {
        return new AttributeEntry(counterName, counter(counterName)).saveTo(block.getAttributes()).getValue();
    }

    /**
     * Register a reference in the document catalog.
     *
     * @param type the type of reference (refs, ids, footnotes, links, images)
     * @param value the value to register
     * @return for refs, the registered node if the id was not already in use, otherwise null
     */
    @SuppressWarnings("unchecked")
    private static Object[] toPair(Object value) {
        return value instanceof java.util.List<?> list ? list.toArray() : (Object[]) value;
    }

    public Object register(String type, Object value) {
        switch (type) {
            case "ids" -> { // deprecated
                Object[] pair = toPair(value);
                String refId = (String) pair[0];
                return register("refs", new Object[] { refId,
                        new Inline(this, "anchor", (String) pair[1], "ref", refId, null, null) });
            }
            case "refs" -> {
                Object[] pair = toPair(value);
                String refId = (String) pair[0];
                AbstractNode ref = (AbstractNode) pair[1];
                if (catalog.getRefs().get(refId) == null) {
                    catalog.getRefs().put(refId, ref);
                    return ref;
                }
                return null;
            }
            case "footnotes" -> {
                catalog.getFootnotes().add((Footnote) value);
                return value;
            }
            default -> {
                if (options.get("catalog_assets") != null && !Boolean.FALSE.equals(options.get("catalog_assets"))) {
                    if (type.equals("images")) {
                        catalog.getImages().add(new ImageReference((String) value,
                                Strings.valueOf(attributes.get("imagesdir"))));
                    } else if (type.equals("links")) {
                        catalog.getLinks().add((String) value);
                    }
                }
                return null;
            }
        }
    }

    /**
     * Register a node in the document catalog under the specified id.
     *
     * @return the registered node if the id was not already in use, otherwise null
     */
    public AbstractNode registerRef(String id, AbstractNode node) {
        return (AbstractNode) register("refs", new Object[] { id, node });
    }

    /**
     * Scan registered references and return the ID of the first reference that matches the specified reference text.
     *
     * @param text The String reference text to compare to the converted reference text of each registered reference.
     * @return the String ID of the first reference with matching reference text or null if no reference is found.
     */
    public String resolveId(String text) {
        if (reftexts != null) {
            return reftexts.get(text);
        } else if (parsed) {
            // reftexts is set eagerly to prevent nested lazy init
            reftexts = new LinkedHashMap<>();
            for (Map.Entry<String, AbstractNode> entry : catalog.getRefs().entrySet()) {
                reftexts.putIfAbsent(entry.getValue().getXreftext(), entry.getKey());
            }
            return reftexts.get(text);
        }
        String resolvedId = null;
        // reftexts is set eagerly to prevent nested lazy init
        Map<String, String> accum = reftexts = new LinkedHashMap<>();
        for (Map.Entry<String, AbstractNode> entry : catalog.getRefs().entrySet()) {
            // NOTE short-circuit early since we're throwing away this table anyway
            String xreftext = entry.getValue().getXreftext();
            if (Objects.equals(xreftext, text)) {
                resolvedId = entry.getKey();
                break;
            }
            accum.putIfAbsent(xreftext, entry.getKey());
        }
        reftexts = null;
        return resolvedId;
    }

    /**
     * Check whether this Document has any child Section objects.
     *
     * @return A Boolean to indicate whether this Document has child Section objects
     */
    @Override
    public boolean hasSections() {
        return nextSectionIndex > 0;
    }

    /**
     * @return whether this document is a multipart book (i.e., a book that has parts), even if it starts with special
     *         sections
     */
    public boolean isMultipart() {
        if (!"book".equals(getDoctype())) {
            return false;
        }
        for (AbstractBlock b : getBlocks()) {
            if (b instanceof Section section) {
                if (section.getLevel() == 0) {
                    return true;
                } else if (!section.isSpecial()) {
                    break;
                }
            }
        }
        return false;
    }

    public boolean hasFootnotes() {
        return !catalog.getFootnotes().isEmpty();
    }

    public List<Footnote> getFootnotes() {
        return catalog.getFootnotes();
    }

    public Callouts getCallouts() {
        return catalog.getCallouts();
    }

    public boolean isNested() {
        return parentDocument != null;
    }

    public boolean isEmbedded() {
        return attributes.containsKey("embedded");
    }

    public boolean hasExtensions() {
        return extensions != null;
    }

    /**
     * Make the raw source for the Document available.
     */
    public String getSource() {
        return reader == null ? null : reader.getSource();
    }

    /**
     * Make the raw source lines for the Document available.
     */
    public List<String> getSourceLines() {
        return reader == null ? null : reader.getSourceLines();
    }

    public boolean isBasebackend(String base) {
        return Objects.equals(attributes.get("basebackend"), base);
    }

    /**
     * Return the doctitle as a String.
     *
     * @return the resolved doctitle as a String or null if a doctitle cannot be resolved
     */
    @Override
    public String getTitle() {
        return getDoctitle();
    }

    /**
     * Set the title on the document header.
     *
     * <p>Set the title of the document header to the specified value. If the header does not exist, it is first
     * created.
     *
     * @param title the String title to assign as the title of the document header
     */
    @Override
    public void setTitle(String title) {
        Section sect = header;
        if (sect == null) {
            sect = header = new Section(this, 0);
            sect.setSectionName("header");
        }
        sect.setTitle(title);
    }

    @Override
    public boolean hasTitle() {
        return getDoctitle() != null;
    }

    /**
     * Resolves the primary title for the document.
     *
     * <p>Searches the locations to find the first non-empty value:
     *
     * <ul>
     * <li>document-level attribute named title</li>
     * <li>header title (known as the document title)</li>
     * <li>title of the first section</li>
     * <li>document-level attribute named untitled-label (if useFallback is set)</li>
     * </ul>
     *
     * <p>If no value can be resolved, null is returned.
     *
     * <p>If sanitize is set, XML elements are removed from the value.
     *
     * @param sanitize whether to remove XML elements from the value
     * @param useFallback whether to use the untitled-label attribute as a fallback
     * @return the resolved title as a String or null if no value can be resolved.
     */
    public String getDoctitle(boolean sanitize, boolean useFallback) {
        String val = resolveDoctitle(useFallback);
        if (val == null) {
            return null;
        }
        if (sanitize && val.contains("<")) {
            return Strings.strip(Strings.squeeze(Rx.gsub(Rx.XML_SANITIZE_RX, val, ""), ' '));
        }
        return val;
    }

    public String getDoctitle() {
        return getDoctitle(false, false);
    }

    /**
     * Resolves the primary title for the document and partitions it into a main title and subtitle.
     *
     * @param separator the separator between the main title and subtitle (or null to use the title-separator attribute
     *            or the default separator, ':')
     * @param sanitize whether to remove XML elements from the value
     * @param useFallback whether to use the untitled-label attribute as a fallback
     * @return the resolved title as a Title or null if no value can be resolved.
     */
    /**
     * @return the document title partitioned into a main title and subtitle (for compatibility with AsciidoctorJ)
     */
    public Title getStructuredDoctitle() {
        return getPartitionedDoctitle(null, false, false);
    }

    /**
     * @return the revision information of this document (the values of the revnumber, revdate and revremark attributes)
     */
    public RevisionInfo getRevisionInfo() {
        return new RevisionInfo(Strings.valueOf(attributes.get("revdate")), Strings.valueOf(attributes.get("revnumber")),
                Strings.valueOf(attributes.get("revremark")));
    }

    public Title getPartitionedDoctitle(String separator, boolean sanitize, boolean useFallback) {
        String val = resolveDoctitle(useFallback);
        if (val == null) {
            return null;
        }
        return new Title(val, sanitize, separator != null ? separator
                : Strings.valueOf(attributes.get("title-separator")));
    }

    private String resolveDoctitle(boolean useFallback) {
        Object val = attributes.get("title");
        if (val == null) {
            Section sect = getFirstSection();
            if (sect != null) {
                return sect.getTitle();
            } else if (!(useFallback && (val = attributes.get("untitled-label")) != null)) {
                return null;
            }
        }
        return val.toString();
    }

    public String getName() {
        return getDoctitle();
    }

    @Override
    public String getXreftext(String xrefstyle) {
        String val = getReftext();
        return val != null && !val.isEmpty() ? val : getTitle();
    }

    /**
     * Convenience method to retrieve the document attribute 'author'.
     *
     * @return the full name of the author as a String
     */
    public String getAuthor() {
        return Strings.valueOf(attributes.get("author"));
    }

    /**
     * Convenience method to retrieve the authors of this document as a List of Author objects.
     *
     * <p>This method is backed by the author-related attributes on the document.
     *
     * @return the authors of this document as a List
     */
    public List<Author> getAuthors() {
        Map<String, Object> attrs = attributes;
        List<Author> authors = new ArrayList<>();
        if (attrs.containsKey("author")) {
            authors.add(new Author(Strings.valueOf(attrs.get("author")), Strings.valueOf(attrs.get("firstname")),
                    Strings.valueOf(attrs.get("middlename")), Strings.valueOf(attrs.get("lastname")),
                    Strings.valueOf(attrs.get("authorinitials")), Strings.valueOf(attrs.get("email"))));
            int numAuthors = Strings.toInt(attrs.get("authorcount"));
            if (numAuthors > 1) {
                int idx = 1;
                while (idx < numAuthors) {
                    idx++;
                    authors.add(new Author(Strings.valueOf(attrs.get("author_" + idx)),
                            Strings.valueOf(attrs.get("firstname_" + idx)),
                            Strings.valueOf(attrs.get("middlename_" + idx)),
                            Strings.valueOf(attrs.get("lastname_" + idx)),
                            Strings.valueOf(attrs.get("authorinitials_" + idx)),
                            Strings.valueOf(attrs.get("email_" + idx))));
                }
            }
        }
        return authors;
    }

    /**
     * Convenience method to retrieve the document attribute 'revdate'.
     *
     * @return the date of last revision for the document as a String
     */
    public String getRevdate() {
        return Strings.valueOf(attributes.get("revdate"));
    }

    public boolean isNotitle() {
        return attributes.containsKey("notitle");
    }

    public boolean isNoheader() {
        return attributes.containsKey("noheader");
    }

    public boolean isNofooter() {
        return attributes.containsKey("nofooter");
    }

    public Section getFirstSection() {
        if (header != null) {
            return header;
        }
        for (AbstractBlock block : blocks) {
            if ("section".equals(block.getContext()) && block instanceof Section section) {
                return section;
            }
        }
        return null;
    }

    public boolean hasHeader() {
        return header != null;
    }

    /**
     * Append a content Block to this Document.
     *
     * <p>If the child block is a Section, assign an index to it.
     *
     * @param block The child Block to append to this parent Block
     * @return The parent Block
     */
    @Override
    public Document append(AbstractBlock block) {
        if ("section".equals(block.getContext()) && block instanceof Section section) {
            assignNumeral(section);
        }
        super.append(block);
        return this;
    }

    /**
     * Internal: Called by the parser after parsing the header and before parsing the body, even if no header is found.
     */
    // QUESTION should we invoke the TreeProcessors here, passing in a phase?
    // QUESTION is finalize_header the right name?
    public Map<String, Object> finalizeHeader(Map<String, Object> unrootedAttributes, boolean headerValid) {
        clearPlaybackAttributes(unrootedAttributes);
        saveAttributes();
        if (!headerValid) {
            unrootedAttributes.put("invalid-header", true);
        }
        return unrootedAttributes;
    }

    public Map<String, Object> finalizeHeader(Map<String, Object> unrootedAttributes) {
        return finalizeHeader(unrootedAttributes, true);
    }

    /**
     * Replay attribute assignments at the block level.
     */
    @SuppressWarnings("unchecked")
    public void playbackAttributes(Map<String, Object> blockAttributes) {
        Object entries = blockAttributes.get(ATTRIBUTE_ENTRIES_KEY);
        if (entries == null) {
            return;
        }
        for (AttributeEntry entry : (List<AttributeEntry>) entries) {
            String name = entry.getName();
            if (entry.isNegate()) {
                attributes.remove(name);
                if (name.equals("compat-mode")) {
                    compatMode = false;
                }
            } else {
                attributes.put(name, entry.getValue());
                if (name.equals("compat-mode")) {
                    compatMode = true;
                }
            }
        }
    }

    /**
     * Restore the attributes to the previously saved state (attributes in header).
     */
    public void restoreAttributes() {
        if (parentDocument == null) {
            catalog.getCallouts().rewind();
        }
        attributes.clear();
        attributes.putAll(headerAttributes);
    }

    /**
     * Set the specified attribute on the document if the name is not locked.
     *
     * <p>If the attribute is locked, null is returned. Otherwise, the value is assigned to the attribute name after
     * first performing attribute substitutions on the value. If the attribute name is 'backend' or 'doctype', then the
     * value of backend-related attributes are updated.
     *
     * @param name the String attribute name
     * @param value the String attribute value; must not be null (optional, default: '')
     * @return the substituted value if the attribute was set or null if it was not because it's locked.
     */
    public String setDocumentAttribute(String name, String value) {
        if (isAttributeLocked(name)) {
            return null;
        }
        if (value == null) {
            value = "";
        }
        if (!value.isEmpty()) {
            value = applyAttributeValueSubs(value);
        }
        // NOTE if header_attributes is set, we're beyond the document header
        if (headerAttributes != null) {
            attributes.put(name, value);
        } else {
            switch (name) {
                case "backend" -> updateBackendAttributes(value,
                        attributesModified.remove("htmlsyntax") && value.equals(backend));
                case "doctype" -> updateDoctypeAttributes(value);
                default -> attributes.put(name, value);
            }
            attributesModified.add(name);
        }
        return value;
    }

    public String setDocumentAttribute(String name) {
        return setDocumentAttribute(name, "");
    }

    /**
     * Delete the specified attribute from the document if the name is not locked.
     *
     * <p>If the attribute is locked, false is returned. Otherwise, the attribute is deleted.
     *
     * @param name the String attribute name
     * @return true if the attribute was deleted, false if it was not because it's locked
     */
    public boolean deleteDocumentAttribute(String name) {
        if (isAttributeLocked(name)) {
            return false;
        }
        attributes.remove(name);
        attributesModified.add(name);
        return true;
    }

    /**
     * Determine if the attribute has been locked by being assigned in document options.
     *
     * @param name The attribute key to check
     * @return true if the attribute is locked, false otherwise
     */
    public boolean isAttributeLocked(String name) {
        return attributeOverrides.containsKey(name);
    }

    /**
     * Assign a value to the specified attribute in the document header.
     *
     * <p>The assignment will be visible when the header attributes are restored, typically between processor phases
     * (e.g., between parse and convert).
     *
     * @param name The String attribute name to assign
     * @param value The Object value to assign to the attribute (default: '')
     * @param overwrite A Boolean indicating whether to assign the attribute if already present in the attributes Map
     *            (default: true)
     * @return a Boolean indicating whether the assignment was performed
     */
    public boolean setHeaderAttribute(String name, Object value, boolean overwrite) {
        Map<String, Object> attrs = headerAttributes != null ? headerAttributes : attributes;
        if (!overwrite && attrs.containsKey(name)) {
            return false;
        }
        attrs.put(name, value);
        return true;
    }

    public boolean setHeaderAttribute(String name, Object value) {
        return setHeaderAttribute(name, value, true);
    }

    public boolean setHeaderAttribute(String name) {
        return setHeaderAttribute(name, "", true);
    }

    /**
     * Convert the AsciiDoc document using the converter associated with this document.
     *
     * @param opts a Map of options (outfile, outdir, standalone, header_footer)
     * @return the converted output as a String (see {@link #convert(Map, Class)} to get an output of another type, such
     *         as the output of a converter for a binary format)
     */
    public String convert(Map<String, Object> opts) {
        return Converter.asString(convertOutput(opts));
    }

    /**
     * Convert the AsciiDoc document using the converter associated with this document.
     *
     * @param opts a Map of options (outfile, outdir, standalone, header_footer)
     * @param outputType the type of the output produced by the converter (e.g., byte[].class)
     * @return the converted output
     * @throws ClassCastException if the output is not of the specified type
     */
    public <T> T convert(Map<String, Object> opts, Class<T> outputType) {
        return outputType.cast(convertOutput(opts));
    }

    public <T> T convert(Class<T> outputType) {
        return convert(Map.of(), outputType);
    }

    /**
     * Convert the AsciiDoc document using the converter associated with this document.
     *
     * @return the output of the converter (a String for a textual format)
     */
    public Object convertOutput(Map<String, Object> opts) {
        return withScopedLogger(() -> doConvertOutput(opts));
    }

    private Object doConvertOutput(Map<String, Object> opts) {
        if (opts == null) {
            opts = Map.of();
        }
        if (timings != null) {
            timings.start("convert");
        }
        if (!parsed) {
            parse();
        }
        if (!(safe >= SafeMode.SERVER.getLevel() || opts.isEmpty())) {
            // QUESTION should we store these on the Document object?
            Object outfile = opts.get("outfile");
            if (outfile != null) {
                attributes.put("outfile", outfile);
            } else {
                attributes.remove("outfile");
            }
            Object outdir = opts.get("outdir");
            if (outdir != null) {
                attributes.put("outdir", outdir);
            } else {
                attributes.remove("outdir");
            }
        }

        // QUESTION should we add extensions that execute before conversion begins?

        Object output = null;
        if ("inline".equals(getDoctype())) {
            AbstractBlock block = !blocks.isEmpty() ? blocks.get(0) : header;
            if (block != null) {
                if (block.getContentModel() == ContentModel.COMPOUND || block.getContentModel() == ContentModel.EMPTY) {
                    getLogger().warn(
                            "no inline candidate; use the inline doctype to convert a single paragragh, verbatim, or raw block");
                } else {
                    output = block.getContent();
                }
            }
        } else {
            String transform;
            if (opts.containsKey("standalone")) {
                transform = Boolean.TRUE.equals(opts.get("standalone")) ? "document" : "embedded";
            } else if (opts.containsKey("header_footer")) {
                transform = Boolean.TRUE.equals(opts.get("header_footer")) ? "document" : "embedded";
            } else {
                transform = Boolean.TRUE.equals(options.get("standalone")) ? "document" : "embedded";
            }
            output = converter.convert(this, transform, null);
        }

        // NOTE the postprocessors only apply to a textual output
        if (parentDocument == null && extensions != null && extensions.hasPostprocessors()
                && (output == null || output instanceof String)) {
            for (ProcessorExtension<Postprocessor> ext : extensions.getPostprocessors()) {
                output = ext.getInstance().process(this, (String) output);
            }
        }

        if (timings != null) {
            timings.record("convert");
        }
        return output;
    }

    @Override
    public String convert() {
        return convert(Map.of());
    }

    /**
     * Write the output to the specified file.
     *
     * <p>If the converter implements {@link Writer}, delegate the work of writing the output to that method. Otherwise,
     * write the output to the specified file. In the latter case, this method ensures the output has a trailing newline
     * if the target is a stream and the output is not empty.
     *
     * <p>A binary output (a byte array) is written as is. An output of another type can only be written by a converter
     * that implements {@link Writer}.
     *
     * @param output The output to write.
     * @param target The file to write, either a String path, a {@link java.io.Writer}, an {@link OutputStream} or an
     *            {@link Appendable}.
     */
    @SuppressWarnings("unchecked")
    public void write(Object output, Object target) {
        if (timings != null) {
            timings.start("write");
        }
        if (converter instanceof Writer<?> writer) {
            ((Writer<Object>) writer).write(output, target);
        } else if (output != null && !(output instanceof String)) {
            if (!(output instanceof byte[] bytes)) {
                throw new IllegalStateException("Cannot write an output of type " + output.getClass().getName()
                        + "; the converter must implement " + Writer.class.getName());
            }
            try {
                Writer.writeBytes(bytes, target);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            writeText((String) output, target);
        }
        if (timings != null) {
            timings.record("write");
        }
    }

    private void writeText(String output, Object target) {
        try {
            if (target instanceof OutputStream stream) {
                // QUESTION should we set encoding using target.set_encoding?
                if (!Strings.isNullOrEmpty(output)) {
                    stream.write(Strings.chomp(output).getBytes(StandardCharsets.UTF_8));
                    // ensure there's a trailing endline
                    stream.write(LF.getBytes(StandardCharsets.UTF_8));
                    stream.flush();
                }
            } else if (target instanceof Appendable appendable) {
                if (!Strings.isNullOrEmpty(output)) {
                    appendable.append(Strings.chomp(output));
                    // ensure there's a trailing endline
                    appendable.append(LF);
                    if (appendable instanceof java.io.Flushable flushable) {
                        flushable.flush();
                    }
                }
            } else {
                Helpers.writeFile(String.valueOf(target), output == null ? "" : output);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if ("manpage".equals(backend) && target instanceof String targetPath && converter instanceof ManPageConverter) {
            ManPageConverter.writeAlternatePages(attributes.get("mannames"),
                    Strings.valueOf(attributes.get("manvolnum")), targetPath);
        }
    }

    @Override
    public String getContent() {
        // NOTE per AsciiDoc-spec, remove the title before converting the body
        attributes.remove("title");
        return super.getContent();
    }

    /**
     * Read the docinfo file(s) for inclusion in the document template.
     *
     * <p>If the docinfo1 attribute is set, read the docinfo.ext file. If the docinfo attribute is set, read the
     * doc-name.docinfo.ext file. If the docinfo2 attribute is set, read both files in that order.
     *
     * @param location The location of the docinfo (e.g., head, footer, etc). (default: head)
     * @param suffix The suffix of the docinfo file(s). If not set, the extension will be set to the outfilesuffix.
     *            (default: null)
     * @return The contents of the docinfo file(s) or empty string if no files are found or the safe mode is secure or
     *         greater.
     */
    public String getDocinfo(String location, String suffix) {
        if (location == null) {
            location = "head";
        }
        List<String> content = null;
        if (safe < SafeMode.SECURE.getLevel()) {
            String qualifier = location.equals("head") ? "" : "-" + location;
            if (suffix == null) {
                suffix = outfilesuffix;
            }

            List<String> docinfo;
            Object docinfoVal = attributes.get("docinfo");
            if (Strings.isNullOrEmpty(docinfoVal)) {
                if (attributes.containsKey("docinfo2")) {
                    docinfo = List.of("private", "shared");
                } else if (attributes.containsKey("docinfo1")) {
                    docinfo = List.of("shared");
                } else {
                    docinfo = docinfoVal != null ? List.of("private") : null;
                }
            } else {
                docinfo = new ArrayList<>();
                for (String it : Strings.split(docinfoVal.toString(), ",")) {
                    docinfo.add(Strings.strip(it));
                }
            }

            if (docinfo != null) {
                content = new ArrayList<>();
                String docinfoFile = "docinfo" + qualifier + suffix;
                String docinfoDir = Strings.valueOf(attributes.get("docinfodir"));
                List<Substitution> docinfoSubs = resolveDocinfoSubs();
                if (docinfo.contains("shared") || docinfo.contains("shared-" + location)) {
                    String docinfoPath = normalizeSystemPath(docinfoFile, docinfoDir);
                    // NOTE normalizing the lines is essential if we're performing substitutions
                    String sharedDocinfo = readAsset(docinfoPath, Map.of("normalize", true));
                    if (sharedDocinfo != null) {
                        content.add(applySubs(sharedDocinfo, docinfoSubs));
                    }
                }

                if (!Strings.isNullOrEmpty(attributes.get("docname"))
                        && (docinfo.contains("private") || docinfo.contains("private-" + location))) {
                    String docinfoPath = normalizeSystemPath(attributes.get("docname") + "-" + docinfoFile,
                            docinfoDir);
                    // NOTE normalizing the lines is essential if we're performing substitutions
                    String privateDocinfo = readAsset(docinfoPath, Map.of("normalize", true));
                    if (privateDocinfo != null) {
                        content.add(applySubs(privateDocinfo, docinfoSubs));
                    }
                }
            }
        }

        // TODO allow document to control whether extension docinfo is contributed
        if (extensions != null && hasDocinfoProcessors(location)) {
            List<String> result = content == null ? new ArrayList<>() : content;
            for (ProcessorExtension<DocinfoProcessor> ext : getDocinfoProcessorExtensions(location)) {
                String extContent = ext.getInstance().process(this);
                if (extContent != null) {
                    result.add(extContent);
                }
            }
            return String.join(LF, result);
        } else if (content != null) {
            return String.join(LF, content);
        }
        return "";
    }

    public String getDocinfo(String location) {
        return getDocinfo(location, null);
    }

    public String getDocinfo() {
        return getDocinfo("head", null);
    }

    @SuppressWarnings("unchecked")
    private List<ProcessorExtension<DocinfoProcessor>> getDocinfoProcessorExtensions(String location) {
        return (List<ProcessorExtension<DocinfoProcessor>>) docinfoProcessorExtensions.get(location);
    }

    public boolean hasDocinfoProcessors(String location) {
        if (location == null) {
            location = "head";
        }
        if (docinfoProcessorExtensions.containsKey(location)) {
            // false means we already performed a lookup and didn't find any
            return !Boolean.FALSE.equals(docinfoProcessorExtensions.get(location));
        } else if (extensions != null && document.getExtensions().hasDocinfoProcessors(location)) {
            List<ProcessorExtension<DocinfoProcessor>> exts = document.getExtensions().getDocinfoProcessors(location);
            docinfoProcessorExtensions.put(location, exts);
            return exts != null;
        }
        docinfoProcessorExtensions.put(location, false);
        return false;
    }

    @Override
    public String toString() {
        return "#<" + getClass().getName() + "@" + System.identityHashCode(this) + " {doctype: "
                + Strings.inspect(getDoctype()) + ", doctitle: " + Strings.inspect(header != null ? header.getTitle()
                        : null) + ", blocks: " + blocks.size() + "}>";
    }

    // --- accessors ---

    /**
     * @return the integer value indicating the level of security that should be enforced while processing this
     *         document. The value must be set in the Document constructor using the safe option.
     */
    public int getSafe() {
        return safe;
    }

    /**
     * @return the Boolean AsciiDoc compatibility mode
     */
    public boolean isCompatMode() {
        return compatMode;
    }

    /**
     * @return the cached value of the backend attribute for this document
     */
    public String getBackend() {
        return backend;
    }

    /**
     * @return the cached value of the doctype attribute for this document
     */
    public String getDoctype() {
        return doctype;
    }

    /**
     * @return the Boolean flag that indicates whether source map information should be tracked by the parser
     */
    public boolean isSourcemap() {
        return sourcemap;
    }

    public void setSourcemap(boolean sourcemap) {
        this.sourcemap = sourcemap;
    }

    /**
     * @return the document catalog
     */
    public Catalog getCatalog() {
        return catalog;
    }

    /**
     * @return the document catalog (alias of catalog for backwards compatibility)
     */
    public Catalog getReferences() {
        return catalog;
    }

    /**
     * @return the Map of document counters
     */
    public Map<String, Object> getCounters() {
        return counters;
    }

    /**
     * @return the level-0 Section (i.e., doctitle). (Only stores the title, not the header attributes).
     */
    public Section getHeader() {
        return header;
    }

    /**
     * @return the String base directory for converting this document. Defaults to directory of the source file. If
     *         the source is a string, defaults to the current directory.
     */
    public String getBaseDir() {
        return baseDir;
    }

    /**
     * @return the Map of resolved options used to initialize this Document
     */
    public Map<String, Object> getOptions() {
        return options;
    }

    /**
     * @return the outfilesuffix defined at the end of the header.
     */
    public String getOutfilesuffix() {
        return outfilesuffix;
    }

    /**
     * @return a reference to the parent Document of this nested document.
     */
    public Document getParentDocument() {
        return parentDocument;
    }

    /**
     * @return the Reader associated with this document
     */
    public Reader getReader() {
        return reader;
    }

    /**
     * @return the PathResolver instance used to resolve paths in this Document.
     */
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    /**
     * @return the Converter associated with this document
     */
    @Override
    public Converter<?> getConverter() {
        return converter;
    }

    /**
     * @return the SyntaxHighlighter associated with this document
     */
    public SyntaxHighlighter getSyntaxHighlighter() {
        return syntaxHighlighter;
    }

    /**
     * @return the activated extension Registry associated with this document.
     */
    public Registry getExtensions() {
        return extensions;
    }

    Set<String> getAttributesModified() {
        return attributesModified;
    }

    // --- internal ---

    /**
     * Internal: Apply substitutions to the attribute value.
     *
     * <p>If the value is an inline passthrough macro (e.g., pass:&lt;subs&gt;[value]), apply the substitutions defined
     * in &lt;subs&gt; to the value, or leave the value unmodified if no substitutions are specified. If the value is
     * not an inline passthrough macro, apply header substitutions to the value.
     *
     * @param value The String attribute value on which to perform substitutions
     * @return The String value with substitutions performed
     */
    private String applyAttributeValueSubs(String value) {
        Matcher m = Rx.match(Rx.ATTRIBUTE_ENTRY_PASS_MACRO_RX, value);
        if (m != null) {
            String subs = m.group(1);
            value = m.group(2);
            if (subs != null) {
                value = applySubs(value, resolvePassSubs(subs));
            }
        } else {
            value = applyHeaderSubs(value);
        }
        return maxAttributeValueSize != null ? limitBytesize(value, maxAttributeValueSize) : value;
    }

    /**
     * Internal: Safely truncates a string to the specified number of bytes.
     *
     * <p>If a multibyte char gets split, the dangling fragment is dropped.
     *
     * @param str The String the truncate.
     * @param max The maximum allowable size of the String, in bytes.
     * @return the String truncated to the specified bytesize.
     */
    private static String limitBytesize(String str, int max) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= max) {
            return str;
        }
        int end = max;
        // back up to the start of a character boundary
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) {
            end--;
        }
        return new String(bytes, 0, end, StandardCharsets.UTF_8);
    }

    /**
     * Internal: Resolve the list of comma-delimited subs to apply to docinfo files.
     *
     * <p>Resolve the list of substitutions from the value of the docinfosubs document attribute, if specified.
     * Otherwise, return a List containing the attributes substitution.
     */
    private List<Substitution> resolveDocinfoSubs() {
        return attributes.containsKey("docinfosubs")
                ? resolveSubs(Strings.valueOf(attributes.get("docinfosubs")), SubsType.BLOCK, null, "docinfo")
                : List.of(Substitution.ATTRIBUTES);
    }

    /**
     * Internal: Create and initialize an instance of the converter for this document.
     */
    // QUESTION is there any additional information we should be passing to the converter?
    private Converter<?> createConverter(String backend, String delegateBackend) {
        Map<String, Object> converterOpts = new LinkedHashMap<>();
        converterOpts.put("document", this);
        converterOpts.put("htmlsyntax", attributes.get("htmlsyntax"));
        Object templateDirs = options.get("template_dirs");
        if (templateDirs == null) {
            templateDirs = options.get("template_dir");
        }
        if (templateDirs != null) {
            converterOpts.put("template_dirs", templateDirs instanceof List<?> ? templateDirs : List.of(templateDirs));
            converterOpts.put("template_cache", options.getOrDefault("template_cache", true));
            converterOpts.put("template_engine", options.get("template_engine"));
            converterOpts.put("template_engine_options", options.get("template_engine_options"));
            converterOpts.put("eruby", options.get("eruby"));
            converterOpts.put("safe", safe);
            if (delegateBackend != null) {
                converterOpts.put("delegate_backend", delegateBackend);
            }
        }
        Object customConverter = options.get("converter");
        if (customConverter != null) {
            ConverterFactory factory = ConverterFactory.custom(Map.of(backend, customConverter));
            return factory.create(backend, converterOpts);
        }
        Object factory = options.get("converter_factory");
        return (factory instanceof ConverterFactory converterFactory ? converterFactory : Converter.getFactory())
                .create(backend, converterOpts);
    }

    /**
     * Internal: Delete any attributes stored for playback.
     */
    private void clearPlaybackAttributes(Map<String, Object> attributes) {
        attributes.remove(ATTRIBUTE_ENTRIES_KEY);
    }

    /**
     * Internal: Branch the attributes so that the original state can be restored at a future time.
     */
    @SuppressWarnings("unchecked")
    private void saveAttributes() {
        Map<String, Object> attrs = attributes;
        String doctitleVal;
        if (!attrs.containsKey("doctitle") && (doctitleVal = getDoctitle()) != null) {
            attrs.put("doctitle", doctitleVal);
        }

        // css-signature cannot be updated after header attributes are processed
        if (id == null) {
            id = Strings.valueOf(attrs.get("css-signature"));
        }

        Object tocVal = attrs.remove("toc2") != null ? "left" : attrs.get("toc");
        if (tocVal != null) {
            // toc-placement allows us to separate position from using fitted slot vs macro
            Object tocPlacementVal = attrs.containsKey("toc-placement") ? attrs.get("toc-placement") : "macro";
            Object tocPositionVal = tocPlacementVal != null && !"auto".equals(tocPlacementVal) ? tocPlacementVal
                    : attrs.get("toc-position");
            if (!(tocVal.toString().isEmpty() && Strings.isNullOrEmpty(tocPositionVal))) {
                String defaultTocPosition = "left";
                // TODO rename toc2 to aside-toc
                String defaultTocClass = "toc2";
                String position = Strings.isNullOrEmpty(tocPositionVal)
                        ? (tocVal.toString().isEmpty() ? defaultTocPosition : tocVal.toString())
                        : tocPositionVal.toString();
                attrs.put("toc", "");
                attrs.put("toc-placement", "auto");
                switch (position) {
                    case "left", "<", "&lt;" -> attrs.put("toc-position", "left");
                    case "right", ">", "&gt;" -> attrs.put("toc-position", "right");
                    case "top", "^" -> attrs.put("toc-position", "top");
                    case "bottom", "v" -> attrs.put("toc-position", "bottom");
                    case "preamble", "macro" -> {
                        attrs.put("toc-position", "content");
                        attrs.put("toc-placement", position);
                        defaultTocClass = null;
                    }
                    default -> {
                        attrs.remove("toc-position");
                        defaultTocClass = null;
                    }
                }
                if (defaultTocClass != null && attrs.get("toc-class") == null) {
                    attrs.put("toc-class", defaultTocClass);
                }
            }
        }

        Object iconsVal = attrs.get("icons");
        if (iconsVal != null && !attrs.containsKey("icontype")) {
            switch (iconsVal.toString()) {
                case "", "font" -> {
                }
                default -> {
                    attrs.put("icons", "");
                    if (!"image".equals(iconsVal)) {
                        attrs.put("icontype", iconsVal);
                    }
                }
            }
        }

        if ((compatMode = attrs.containsKey("compat-mode")) && attrs.containsKey("language")) {
            attrs.put("source-language", attrs.get("language"));
        }

        if (parentDocument == null) {
            Object basebackend = attrs.get("basebackend");
            if ("html".equals(basebackend)) {
                // QUESTION should we allow source-highlighter to be disabled in AsciiDoc table cell?
                Object syntaxHlName = attrs.get("source-highlighter");
                if (syntaxHlName != null && attrs.get(syntaxHlName + "-unavailable") == null) {
                    Map<String, Object> hlOpts = Map.of("document", this);
                    Object syntaxHlFactory = options.get("syntax_highlighter_factory");
                    Object syntaxHls = options.get("syntax_highlighters");
                    if (syntaxHlFactory instanceof SyntaxHighlighterFactory factory) {
                        syntaxHighlighter = factory.create(syntaxHlName.toString(), backend, hlOpts);
                    } else if (syntaxHls instanceof Map<?, ?> registry) {
                        syntaxHighlighter = SyntaxHighlighterFactory.proxy((Map<String, Object>) registry)
                                .create(syntaxHlName.toString(), backend, hlOpts);
                    } else {
                        syntaxHighlighter = SyntaxHighlighter.getFactory().create(syntaxHlName.toString(), backend,
                                hlOpts);
                    }
                }
            // enable toc and sectnums (i.e., numbered) by default in DocBook backend
            } else if ("docbook".equals(basebackend)) {
                // NOTE the attributes_modified should go away once we have a proper attribute storage & tracking
                // facility
                if (!(isAttributeLocked("toc") || attributesModified.contains("toc"))) {
                    attrs.put("toc", "");
                }
                if (!(isAttributeLocked("sectnums") || attributesModified.contains("sectnums"))) {
                    attrs.put("sectnums", "");
                }
            }

            // NOTE pin the outfilesuffix after the header is parsed
            outfilesuffix = Strings.valueOf(attrs.get("outfilesuffix"));

            // unfreeze "flexible" attributes
            for (String name : FLEXIBLE_ATTRIBUTES) {
                // turning a flexible attribute off should be permanent
                // (we may need more config if that's not always the case)
                if (attributeOverrides.containsKey(name) && attributeOverrides.get(name) != null) {
                    attributeOverrides.remove(name);
                }
            }
        }

        headerAttributes = new LinkedHashMap<>(attrs);
    }

    /**
     * Internal: Assign the local and document datetime attributes, which includes localdate, localyear, localtime,
     * localdatetime, docdate, docyear, doctime, and docdatetime. Honor the SOURCE_DATE_EPOCH environment variable, if
     * set.
     */
    private static void fillDatetimeAttributes(Map<String, Object> attrs, Object inputMtime) {
        // See https://reproducible-builds.org/specs/source-date-epoch/
        ZonedDateTime sourceDateEpoch = null;
        ZonedDateTime now;
        String sourceDateEpochEnv = System.getenv("SOURCE_DATE_EPOCH");
        if (sourceDateEpochEnv != null && !sourceDateEpochEnv.isEmpty()) {
            now = sourceDateEpoch = Instant.ofEpochSecond(Long.parseLong(sourceDateEpochEnv)).atZone(ZoneOffset.UTC);
        } else {
            now = ZonedDateTime.now();
        }
        Object localdate = attrs.get("localdate");
        if (localdate != null) {
            if (attrs.get("localyear") == null) {
                attrs.put("localyear", localdate.toString().indexOf('-') == 4 ? localdate.toString().substring(0, 4)
                        : null);
            }
        } else {
            localdate = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
            attrs.put("localdate", localdate);
            if (attrs.get("localyear") == null) {
                attrs.put("localyear", String.valueOf(now.getYear()));
            }
        }
        // %Z is OS dependent and may contain characters that aren't UTF-8 encoded (see asciidoctor#2770 and
        // asciidoctor.js#23)
        if (attrs.get("localtime") == null) {
            attrs.put("localtime", formatTime(now));
        }
        Object localtime = attrs.get("localtime");
        if (attrs.get("localdatetime") == null) {
            attrs.put("localdatetime", localdate + " " + localtime);
        }
        // docdate, doctime and docdatetime should default to localdate, localtime and localdatetime if not otherwise
        // set
        ZonedDateTime mtime;
        if (sourceDateEpoch != null) {
            mtime = sourceDateEpoch;
        } else if (inputMtime instanceof FileTime fileTime) {
            mtime = fileTime.toInstant().atZone(ZoneId.systemDefault());
        } else if (inputMtime instanceof ZonedDateTime zoned) {
            mtime = zoned;
        } else {
            mtime = now;
        }
        Object docdate = attrs.get("docdate");
        if (docdate != null) {
            if (attrs.get("docyear") == null) {
                attrs.put("docyear", docdate.toString().indexOf('-') == 4 ? docdate.toString().substring(0, 4) : null);
            }
        } else {
            docdate = mtime.format(DateTimeFormatter.ISO_LOCAL_DATE);
            attrs.put("docdate", docdate);
            if (attrs.get("docyear") == null) {
                attrs.put("docyear", String.valueOf(mtime.getYear()));
            }
        }
        // %Z is OS dependent and may contain characters that aren't UTF-8 encoded (see asciidoctor#2770 and
        // asciidoctor.js#23)
        if (attrs.get("doctime") == null) {
            attrs.put("doctime", formatTime(mtime));
        }
        Object doctime = attrs.get("doctime");
        if (attrs.get("docdatetime") == null) {
            attrs.put("docdatetime", docdate + " " + doctime);
        }
    }

    private static String formatTime(ZonedDateTime time) {
        String clock = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        return time.getOffset().getTotalSeconds() == 0 ? clock + " UTC"
                : clock + " " + time.format(DateTimeFormatter.ofPattern("xx"));
    }

    /**
     * Internal: Update the backend attributes to reflect a change in the active backend.
     *
     * <p>This method also handles updating the related doctype attributes if the doctype attribute is assigned at the
     * time this method is called.
     *
     * @return the resolved String backend if updated, null otherwise.
     */
    private String updateBackendAttributes(String newBackend, boolean init) {
        if (!(init || !newBackend.equals(backend))) {
            return null;
        }
        String currentBackend = backend;
        Map<String, Object> attrs = attributes;
        Object currentBasebackend = attrs.get("basebackend");
        String currentDoctype = doctype;
        String actualBackend = null;
        if (newBackend.contains(":")) {
            String[] parts = Strings.partition(newBackend, ":");
            actualBackend = parts[0];
            newBackend = parts[2];
        }
        if (newBackend.startsWith("xhtml")) {
            attrs.put("htmlsyntax", "xml");
            newBackend = newBackend.substring(1);
        } else if (newBackend.startsWith("html")) {
            if (attrs.get("htmlsyntax") == null) {
                attrs.put("htmlsyntax", "html");
            }
        }
        newBackend = BACKEND_ALIASES.getOrDefault(newBackend, newBackend);
        String delegateBackend = null;
        if (actualBackend != null) {
            delegateBackend = newBackend;
            newBackend = actualBackend;
        }
        if (currentDoctype != null) {
            if (currentBackend != null) {
                attrs.remove("backend-" + currentBackend);
                attrs.remove("backend-" + currentBackend + "-doctype-" + currentDoctype);
            }
            attrs.put("backend-" + newBackend + "-doctype-" + currentDoctype, "");
            attrs.put("doctype-" + currentDoctype, "");
        } else if (currentBackend != null) {
            attrs.remove("backend-" + currentBackend);
        }
        attrs.put("backend-" + newBackend, "");
        // QUESTION should we defer the @backend assignment until after the converter is created?
        backend = newBackend;
        attrs.put("backend", newBackend);
        // (re)initialize converter
        Converter<?> newConverter = createConverter(newBackend, delegateBackend);
        String newBasebackend;
        String newFiletype;
        if (newConverter instanceof BackendTraits traits) {
            newBasebackend = traits.getBasebackend();
            newFiletype = traits.getFiletype();
            String htmlsyntax = traits.getHtmlsyntax();
            if (htmlsyntax != null) {
                attrs.put("htmlsyntax", htmlsyntax);
            }
            if (init) {
                if (attrs.get("outfilesuffix") == null) {
                    attrs.put("outfilesuffix", traits.getOutfilesuffix());
                }
            } else if (!isAttributeLocked("outfilesuffix")) {
                attrs.put("outfilesuffix", traits.getOutfilesuffix());
            }
        } else if (newConverter != null) {
            BackendTraits.Values backendTraits = BackendTraits.derive(newBackend, null);
            newBasebackend = backendTraits.basebackend();
            newFiletype = backendTraits.filetype();
            if (init) {
                if (attrs.get("outfilesuffix") == null) {
                    attrs.put("outfilesuffix", backendTraits.outfilesuffix());
                }
            } else if (!isAttributeLocked("outfilesuffix")) {
                attrs.put("outfilesuffix", backendTraits.outfilesuffix());
            }
        } else {
            // NOTE ideally we shouldn't need the converter before the converter phase, but we do
            throw new UnsupportedOperationException("asciidoctor: FAILED: missing converter for backend '"
                    + newBackend + "'. Processing aborted.");
        }
        converter = newConverter;
        Object currentFiletype = attrs.get("filetype");
        if (currentFiletype != null) {
            attrs.remove("filetype-" + currentFiletype);
        }
        attrs.put("filetype", newFiletype);
        attrs.put("filetype-" + newFiletype, "");
        Integer pageWidth = DEFAULT_PAGE_WIDTHS.get(newBasebackend);
        if (pageWidth != null) {
            attrs.put("pagewidth", pageWidth);
        } else {
            attrs.remove("pagewidth");
        }
        if (!Objects.equals(newBasebackend, currentBasebackend)) {
            if (currentDoctype != null) {
                if (currentBasebackend != null) {
                    attrs.remove("basebackend-" + currentBasebackend);
                    attrs.remove("basebackend-" + currentBasebackend + "-doctype-" + currentDoctype);
                }
                attrs.put("basebackend-" + newBasebackend + "-doctype-" + currentDoctype, "");
            } else if (currentBasebackend != null) {
                attrs.remove("basebackend-" + currentBasebackend);
            }
            attrs.put("basebackend-" + newBasebackend, "");
            attrs.put("basebackend", newBasebackend);
        }
        return newBackend;
    }

    /**
     * Internal: Update the doctype and backend attributes to reflect a change in the active doctype.
     *
     * @return the String doctype if updated, null otherwise.
     */
    private String updateDoctypeAttributes(String newDoctype) {
        if (newDoctype == null || newDoctype.equals(doctype)) {
            return null;
        }
        String currentBackend = backend;
        Map<String, Object> attrs = attributes;
        Object currentBasebackend = attrs.get("basebackend");
        String currentDoctype = doctype;
        if (currentDoctype != null) {
            attrs.remove("doctype-" + currentDoctype);
            if (currentBackend != null) {
                attrs.remove("backend-" + currentBackend + "-doctype-" + currentDoctype);
                attrs.put("backend-" + currentBackend + "-doctype-" + newDoctype, "");
            }
            if (currentBasebackend != null) {
                attrs.remove("basebackend-" + currentBasebackend + "-doctype-" + currentDoctype);
                attrs.put("basebackend-" + currentBasebackend + "-doctype-" + newDoctype, "");
            }
        } else {
            if (currentBackend != null) {
                attrs.put("backend-" + currentBackend + "-doctype-" + newDoctype, "");
            }
            if (currentBasebackend != null) {
                attrs.put("basebackend-" + currentBasebackend + "-doctype-" + newDoctype, "");
            }
        }
        attrs.put("doctype-" + newDoctype, "");
        doctype = newDoctype;
        attrs.put("doctype", newDoctype);
        return newDoctype;
    }

    /**
     * Calls the specified action with the logger of this document (if any) bound to the current scope.
     */
    private <T> T withScopedLogger(Supplier<T> action) {
        return scopedLogger == null ? action.get() : LoggerManager.callWithLogger(scopedLogger, action);
    }
}
