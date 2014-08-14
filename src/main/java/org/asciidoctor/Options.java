package org.asciidoctor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Options {

    public static final String IN_PLACE = "in_place";
    public static final String ATTRIBUTES = "attributes";
    public static final String HEADER_FOOTER = "header_footer";
    public static final String TEMPLATE_DIRS = "template_dirs";
    public static final String TEMPLATE_ENGINE = "template_engine";
    public static final String TO_FILE = "to_file";
    public static final String TO_DIR = "to_dir";
    public static final String MKDIRS = "mkdirs";
    public static final String SAFE = "safe";
    public static final String ERUBY = "eruby";
    public static final String COMPACT = "compact";
    public static final String DESTINATION_DIR = "destination_dir";
    public static final String BACKEND = "backend";
    public static final String DOCTYPE = "doctype";
    public static final String BASEDIR = "base_dir";
    public static final String TEMPLATE_CACHE = "template_cache";
    public static final String SOURCE = "source";

    private Map<String, Object> options = new HashMap<String, Object>();

    public Options() {
        super();
    }

    public Options(Map<String, Object> options) {
        this.options = options;
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

    public void setHeaderFooter(boolean headerFooter) {
        this.options.put(HEADER_FOOTER, headerFooter);
    }

    public void setTemplateDirs(String... templateDirs) {

        if (!this.options.containsKey(TEMPLATE_DIRS)) {
            this.options.put(TEMPLATE_DIRS, new ArrayList<Object>());
        }

        List<Object> allTemplateDirs = (List<Object>) this.options.get(TEMPLATE_DIRS);
        allTemplateDirs.addAll(Arrays.asList(templateDirs));
    }

    public void setTemplateEngine(String templateEngine) {
        this.options.put(TEMPLATE_ENGINE, templateEngine);
    }

    public void setToFile(String toFile) {
        this.options.put(TO_FILE, toFile);
    }

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

    public void setEruby(String eruby) {
        this.options.put(ERUBY, eruby);
    }

    public void setCompact(boolean compact) {
        this.options.put(COMPACT, compact);
    }

    public void setDestinationDir(String destinationDir) {
        this.options.put(DESTINATION_DIR, destinationDir);
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

    public void setOption(String optionName, Object optionValue) {
        this.options.put(optionName, optionValue);
    }

    public Map<String, Object> map() {
        return this.options;
    }

}
