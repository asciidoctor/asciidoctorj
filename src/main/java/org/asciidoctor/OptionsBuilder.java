package org.asciidoctor;

import java.io.File;
import java.util.Map;

public class OptionsBuilder {

    private Options options = new Options();

    private OptionsBuilder() {
        super();
    }

    /**
     * Creates options builder instance.
     * 
     * @return options builder instance.
     */
    public static OptionsBuilder options() {
        return new OptionsBuilder();
    }

    /**
     * Sets backend option.
     * 
     * @param backend
     *            value.
     * @return this instance.
     */
    public OptionsBuilder backend(String backend) {
        this.options.setBackend(backend);
        return this;
    }

    /**
     * Sets doctype option.
     * 
     * @param docType
     *            value.
     * @return this instance.
     */
    public OptionsBuilder docType(String docType) {
        this.options.setDocType(docType);
        return this;
    }

    /**
     * Sets in place attribute.
     * 
     * @param inPlace
     *            value.
     * @return this instance.
     */
    public OptionsBuilder inPlace(boolean inPlace) {
        this.options.setInPlace(inPlace);
        return this;
    }

    /**
     * Sets header footer attribute.
     * 
     * @param headerFooter
     *            value.
     * @return this instance.
     */
    public OptionsBuilder headerFooter(boolean headerFooter) {
        this.options.setHeaderFooter(headerFooter);
        return this;
    }

    /**
     * Sets template directory.
     * 
     * @param templateDir
     *            directory where templates are stored.
     * @return this instance.
     */
    public OptionsBuilder templateDir(File templateDir) {
        this.options.setTemplateDirs(templateDir.getAbsolutePath());
        return this;
    }

    /**
     * Sets template directories.
     * 
     * @param templateDir
     *            directories where templates are stored.
     * @return this instance.
     */
    public OptionsBuilder templateDirs(File... templateDirs) {
        for (File templateDir : templateDirs) {
            this.options.setTemplateDirs(templateDir.getAbsolutePath());
        }
        return this;
    }

    /**
     * Sets the template engine.
     * 
     * @param templateEngine
     *            used to render the document.
     * @return this instance.
     */
    public OptionsBuilder templateEngine(String templateEngine) {
        this.options.setTemplateEngine(templateEngine);
        return this;
    }

    /**
     * Sets if Asciidoctor should use template cache or not.
     * 
     * @param templateCache
     *            true if template cache is required, false otherwise.
     * @return this instance.
     */
    public OptionsBuilder templateCache(boolean templateCache) {
        this.options.setTemplateCache(templateCache);
        return this;
    }

    /**
     * Sets attributes used for rendering input.
     * 
     * @param attributes
     *            map.
     * @return this instance.
     */
    public OptionsBuilder attributes(Map<String, Object> attributes) {
        this.options.setAttributes(attributes);
        return this;
    }

    /**
     * Sets attributes used for rendering input.
     * 
     * @param attributes
     *            map.
     * @return this instance.
     */
    public OptionsBuilder attributes(Attributes attributes) {
        this.options.setAttributes(attributes.map());
        return this;
    }
    
    /**
     * Sets attributes used for rendering input.
     * 
     * @param attributes
     *            builder.
     * @return this instance.
     */
    public OptionsBuilder attributes(AttributesBuilder attributes) {
        this.options.setAttributes(attributes.asMap());
        return this;
    }

    /**
     * Sets to file value. This is the destination file name.
     * 
     * @param toFile
     *            name of output file.
     * @return this instance.
     */
    public OptionsBuilder toFile(boolean toFile) {
        this.options.setToFile(toFile);
        return this;
    }

    /**
     * Sets to file value. This is the destination file name.
     * 
     * @param toFile
     *            name of output file.
     * @return this instance.
     */
    public OptionsBuilder toFile(File toFile) {
        this.options.setToFile(toFile.getPath());
        return this;
    }

    /**
     * Sets to dir value. This is the destination directory.
     * 
     * @param directory
     *            where output is generated.
     * @return this instance.
     */
    public OptionsBuilder toDir(File directory) {
        this.options.setToDir(directory.getAbsolutePath());
        return this;
    }

    /**
     * Sets if asciidoctor should create output directory if it does not exist or not.
     * 
     * @param mkDirs
     *            true if directory must be created, false otherwise.
     * @return this instance.
     */
    public OptionsBuilder mkDirs(boolean mkDirs) {
        this.options.setMkDirs(mkDirs);
        return this;
    }

    /**
     * Sets the safe mode.
     * 
     * @param safeMode
     *            to run asciidoctor.
     * @return this instance.
     */
    public OptionsBuilder safe(SafeMode safeMode) {
        this.options.setSafe(safeMode);
        return this;
    }

    /**
     * Sets eruby implementation.
     * 
     * @param eruby
     *            implementation.
     * @return this instance.
     */
    public OptionsBuilder eruby(String eruby) {
        this.options.setEruby(eruby);
        return this;
    }

    /**
     * Compact the output removing blank lines.
     * 
     * @param compact
     *            value.
     * @return this instance.
     */
    public OptionsBuilder compact(boolean compact) {
        this.options.setCompact(compact);
        return this;
    }

    /**
     * Destination output directory.
     * 
     * @param destinationDir
     *            destination directory.
     * @return this instance.
     */
    public OptionsBuilder destinationDir(File destinationDir) {
        this.options.setDestinationDir(destinationDir.getAbsolutePath());
        return this;
    }

    /**
     * Sets a custom or unlisted option.
     * 
     * @param option
     *            name.
     * @param value
     *            for given option.
     * @return this instance.
     */
    public OptionsBuilder option(String option, Object value) {
        this.options.setOption(option, value);
        return this;
    }

    /**
     * Sets base dir for working directory.
     * 
     * @param baseDir
     *            working directory.
     * @return this instance.
     */
    public OptionsBuilder baseDir(File baseDir) {
        this.options.setBaseDir(baseDir.getAbsolutePath());
        return this;
    }

    /**
     * Gets a map with configured options.
     * 
     * @return map with all options. By default an empty map is returned.
     */
    public Map<String, Object> asMap() {
        return this.options.map();
    }

    public Options get() {
        return this.options;
    }

}
