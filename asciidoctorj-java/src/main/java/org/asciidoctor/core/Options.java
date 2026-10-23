package org.asciidoctor.core;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The options passed to the processor.
 *
 * <p>The options can be passed to the processor as an instance of this class (created using {@link #builder()}),
 * e.g., {@link Asciidoctor#convert(Object, Options)}, or as a Map, which can be created using {@link #of(Object...)}.
 */
public class Options {
    public static final String ATTRIBUTES = "attributes";
    public static final String BACKEND = "backend";
    public static final String BASE_DIR = "base_dir";
    /** Alias of {@link #BASE_DIR} (for compatibility with AsciidoctorJ). */
    public static final String BASEDIR = BASE_DIR;
    public static final String COMPACT = "compact";
    public static final String ERUBY = "eruby";
    public static final String IN_PLACE = "in_place";
    public static final String SOURCE = "source";
    public static final String TEMPLATE_CACHE = "template_cache";
    public static final String TEMPLATE_DIRS = "template_dirs";
    public static final String TEMPLATE_ENGINE = "template_engine";
    public static final String TRACE = "trace";
    public static final String WARNINGS = "warnings";
    public static final String CATALOG_ASSETS = "catalog_assets";
    public static final String CONVERTER = "converter";
    public static final String DOCTYPE = "doctype";
    public static final String EXTENSION_REGISTRY = "extension_registry";
    public static final String EXTENSIONS = "extensions";
    public static final String HEADER_FOOTER = "header_footer";
    public static final String LOGGER = "logger";
    public static final String MKDIRS = "mkdirs";
    public static final String PARSE = "parse";
    public static final String PARSE_HEADER_ONLY = "parse_header_only";
    public static final String SAFE = "safe";
    public static final String SOURCEMAP = "sourcemap";
    public static final String STANDALONE = "standalone";
    public static final String TIMINGS = "timings";
    public static final String TO_DIR = "to_dir";
    public static final String TO_FILE = "to_file";

    private final Map<String, Object> options = new LinkedHashMap<>();

    public Options() {
    }

    public static OptionsBuilder builder() {
        return new OptionsBuilder();
    }

    public void setInPlace(boolean inPlace) {
        options.put(IN_PLACE, inPlace);
    }

    public void setAttributes(Attributes attributes) {
        options.put(ATTRIBUTES, attributes.map());
    }

    public void setAttributes(Map<String, Object> attributes) {
        options.put(ATTRIBUTES, attributes);
    }

    /**
     * Sets whether the output should be written to a file (true) or returned as a String (false).
     */
    public void setToFile(boolean toFile) {
        options.put(TO_FILE, toFile);
    }

    /**
     * Sets the path of the file the output is written to.
     */
    public void setToFile(String toFile) {
        options.put(TO_FILE, toFile);
    }

    public void setToStream(OutputStream toStream) {
        options.put(TO_FILE, toStream);
    }

    public void setStandalone(boolean standalone) {
        options.put(STANDALONE, standalone);
    }

    public void setTemplateDirs(String... templateDirs) {
        options.put(TEMPLATE_DIRS, new ArrayList<>(List.of(templateDirs)));
    }

    public void setTemplateEngine(String templateEngine) {
        options.put(TEMPLATE_ENGINE, templateEngine);
    }

    public void setTemplateCache(boolean templateCache) {
        options.put(TEMPLATE_CACHE, templateCache);
    }

    public void setSafe(SafeMode safeMode) {
        options.put(SAFE, safeMode.getLevel());
    }

    public void setToDir(String toDir) {
        options.put(TO_DIR, toDir);
    }

    public void setMkDirs(boolean mkDirs) {
        options.put(MKDIRS, mkDirs);
    }

    public void setEruby(String eruby) {
        options.put(ERUBY, eruby);
    }

    public void setCompact(boolean compact) {
        options.put(COMPACT, compact);
    }

    public void setDocType(String docType) {
        options.put(DOCTYPE, docType);
    }

    public void setBackend(String backend) {
        options.put(BACKEND, backend);
    }

    public void setBaseDir(String baseDir) {
        options.put(BASE_DIR, baseDir);
    }

    public void setSourcemap(boolean sourcemap) {
        options.put(SOURCEMAP, sourcemap);
    }

    public void setCatalogAssets(boolean catalogAssets) {
        options.put(CATALOG_ASSETS, catalogAssets);
    }

    public void setParse(boolean parse) {
        options.put(PARSE, parse);
    }

    public void setParseHeaderOnly(boolean parseHeaderOnly) {
        options.put(PARSE_HEADER_ONLY, parseHeaderOnly);
    }

    public void setOption(String optionName, Object optionValue) {
        options.put(optionName, optionValue);
    }

    /**
     * @return the options as a (mutable) Map (e.g., to be passed to {@link Asciidoctor#convert(Object, Map)})
     */
    public Map<String, Object> map() {
        return options;
    }

    /**
     * Creates a mutable, ordered Map from the specified key/value pairs. Unlike {@link Map#of}, null values are
     * permitted.
     *
     * @param keyValues alternating keys (Strings) and values
     * @return a new mutable Map
     */
    public static Map<String, Object> of(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("expecting an even number of arguments");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    /**
     * Resolves the value of the attributes option as a mutable Map.
     *
     * <p>The attributes may be specified as a Map, a List of key=value Strings or a String of space-separated key=value
     * pairs (where spaces in values may be escaped with a backslash).
     *
     * @param attrs the value of the attributes option
     * @return a new mutable Map of attributes
     */
    public static Map<String, Object> resolveAttributes(Object attrs) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (attrs == null) {
            return result;
        } else if (attrs instanceof Map<?, ?> map) {
            map.forEach((key, value) -> result.put(String.valueOf(key), value));
        } else if (attrs instanceof List<?> list) {
            for (Object entry : list) {
                String[] parts = Strings.partition(String.valueOf(entry), "=");
                result.put(parts[0], parts[2]);
            }
        } else if (attrs instanceof String str) {
            // condense and convert non-escaped spaces to null, unescape escaped spaces, then split on null
            String normalized = Rx.gsub(Rx.SPACE_DELIMITER_RX, str, m -> m.group(1) + Asciidoctor.NULL);
            normalized = Rx.gsub(Rx.ESCAPED_SPACE_RX, normalized, m -> m.group(1));
            for (String entry : Strings.split(normalized, Asciidoctor.NULL)) {
                String[] parts = Strings.partition(entry, "=");
                result.put(parts[0], parts[2]);
            }
        } else {
            throw new IllegalArgumentException("illegal type for attributes option: " + attrs.getClass());
        }
        return result;
    }
}
