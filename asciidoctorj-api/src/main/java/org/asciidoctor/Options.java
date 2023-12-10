package org.asciidoctor;

import java.io.OutputStream;
import java.util.*;

/**
 * AsciidoctorJ conversion options.
 * <p>
 * See <a href="https://docs.asciidoctor.org/asciidoctor/latest/api/options/">https://docs.asciidoctor.org/asciidoctor/latest/api/options/</a> for further
 * details.
 */
public class Options {

    public static final String IN_PLACE = "in_place";
    public static final String WARNINGS = "warnings";
    public static final String TIMINGS = "timings";
    public static final String ATTRIBUTES = "attributes";
    public static final String TEMPLATE_DIRS = "template_dirs";
    public static final String TEMPLATE_ENGINE = "template_engine";
    public static final String TO_FILE = "to_file";
    public static final String TO_DIR = "to_dir";
    public static final String MKDIRS = "mkdirs";
    public static final String SAFE = "safe";
    public static final String SOURCEMAP = "sourcemap";
    public static final String STANDALONE = "standalone";
    public static final String ERUBY = "eruby";
    public static final String CATALOG_ASSETS = "catalog_assets";
    public static final String COMPACT = "compact";
    public static final String BACKEND = "backend";
    public static final String DOCTYPE = "doctype";
    public static final String BASEDIR = "base_dir";
    public static final String TRACE = "trace";
    public static final String TEMPLATE_CACHE = "template_cache";
    public static final String SOURCE = "source";
    public static final String PARSE = "parse";
    public static final String PARSE_HEADER_ONLY = "parse_header_only";

    private final Map<String, Object> options = new HashMap<>();

    Options() {
    }

    /**
     * @return Empty OptionsBuilder instance.
     */
    public static OptionsBuilder builder() {
        return new OptionsBuilder();
    }

    public void setInPlace(boolean inPlace) {
        this.options.put(IN_PLACE, inPlace);
    }

    public void setAttributes(Attributes attributes) {
        this.options.put(ATTRIBUTES, attributes.map());
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.options.put(ATTRIBUTES, attributes);
    }

    /**
     * Toggle including header and footer into the output.
     *
     * @param standalone <code>true</code> to generate a standalone output document
     *                   (which includes the shell around the body content, such
     *                   as the header and footer).
     *                   Defaults to <code>true</code> when converting a file only,
     *                   otherwise is <code>false</code>.
     */
    public void setStandalone(boolean standalone) {
        this.options.put(STANDALONE, standalone);
    }

    public void setTemplateDirs(String... templateDirs) {

        if (!this.options.containsKey(TEMPLATE_DIRS)) {
            this.options.put(TEMPLATE_DIRS, new ArrayList<>());
        }

        List<Object> allTemplateDirs = (List<Object>) this.options.get(TEMPLATE_DIRS);
        allTemplateDirs.addAll(Arrays.asList(templateDirs));
    }

    public void setTemplateEngine(String templateEngine) {
        this.options.put(TEMPLATE_ENGINE, templateEngine);
    }

    /**
     * Enable writing output to a file. The file includes header and footer by default.
     *
     * @param toFile The path to the output file. If the path is not absolute, it is
     *               interpreted relative to what was set via {@link #setToDir(String)}}
     *               or {@link #setBaseDir(String)}}, in that order.
     *
     */
    public void setToFile(String toFile) {
        this.options.put(TO_FILE, toFile);
    }

    public void setToStream(OutputStream toStream) {
        this.options.put(TO_FILE, toStream);
    }

    /**
     * Toggle writing output to a file.
     *
     * @param toFile If <code>true</true>, write output to a file in the same directory
     *               as the input file, including header and footer into the output. If
     *               <code>false</code>, return output as a string without any header or
     *               footer. The default header and footer visibility can be overridden
     *               using {@link #setStandalone(boolean)}.
     *
     */
    public void setToFile(boolean toFile) {
        this.options.put(TO_FILE, toFile);
    }

    public void setToDir(String toDir) {
        this.options.put(TO_DIR, toDir);
    }

    public void setMkDirs(boolean mkDirs) {
        this.options.put(MKDIRS, mkDirs);
    }

    /**
     * Safe method calls safeMode.getLevel() to put the required level.
     *
     * @param safeMode
     *            enum.
     */
    public void setSafe(SafeMode safeMode) {
        this.options.put(SAFE, safeMode.getLevel());
    }

    /**
     * Keeps track of the file and line number for each parsed block. (Useful for tooling applications where the association between the converted output and the source file is important).
     *
     * @param sourcemap
     *            value.
     */
    public void setSourcemap(boolean sourcemap) {
        this.options.put(SOURCEMAP, sourcemap);
    }

    public void setEruby(String eruby) {
        this.options.put(ERUBY, eruby);
    }

    /**
     * If true, tells the parser to capture images and links in the reference table. (Normally only IDs, footnotes and indexterms are included). The reference table is available via the references property on the document AST object. (Experimental).
     *
     * @param catalogAssets
     *            value.
     */
    public void setCatalogAssets(boolean catalogAssets) {
        this.options.put(CATALOG_ASSETS, catalogAssets);
    }

    public void setCompact(boolean compact) {
        this.options.put(COMPACT, compact);
    }

    public void setBackend(String backend) {
        this.options.put(BACKEND, backend);
    }

    public void setDocType(String docType) {
        this.options.put(DOCTYPE, docType);
    }

    public void setBaseDir(String baseDir) {
        this.options.put(BASEDIR, baseDir);
    }

    public void setTemplateCache(boolean templateCache) {
        this.options.put(TEMPLATE_CACHE, templateCache);
    }

    /**
     * If true, the source is parsed eagerly (i.e., as soon as the source is passed to the load or load_file API). If false, parsing is deferred until the parse method is explicitly invoked.
     *
     * @param parse
     *            value.
     */
    public void setParse(boolean parse) {
        this.options.put(PARSE, parse);
    }

    public void setParseHeaderOnly(boolean parseHeaderOnly) {
        this.options.put(PARSE_HEADER_ONLY, parseHeaderOnly);
    }

    public void setOption(String optionName, Object optionValue) {
        this.options.put(optionName, optionValue);
    }

    /**
     * @deprecated For internal use only.
     */
    @Deprecated
    public Map<String, Object> map() {
        return this.options;
    }

}
