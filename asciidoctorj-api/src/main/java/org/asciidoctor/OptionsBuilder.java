package org.asciidoctor;

import java.io.File;
import java.io.OutputStream;

/**
 * Fluent Options API for AsciidoctorJ.
 *
 * @see org.asciidoctor.Options
 */
public class OptionsBuilder {

    private final Options options = new Options();

    OptionsBuilder() {
        super();
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
     * Valid options are <code>article</code>, <code>book</code>,
     * <code>manpage</code> and <code>inline</code>.
     * Default <code>article</code>.
     *
     * @param docType value.
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
     * Sets standalone option.
     *
     * @param standalone <code>true</code> to generate a standalone output document
     *                   (which includes the shell around the body content, such
     *                   as the header and footer).
     *                   Defaults to <code>true</code> when converting a file only,
     *                   otherwise is <code>false</code>.
     * @return this instance.
     */
    public OptionsBuilder standalone(boolean standalone) {
        this.options.setStandalone(standalone);
        return this;
    }

    /**
     * Sets template directories.
     * 
     * @param templateDirs
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
    public OptionsBuilder attributes(Attributes attributes) {
        this.options.setAttributes(attributes);
        return this;
    }

    /**
     * Sets to file value. This toggles writing output to a file or returning output
     * as a string. If writing to a string, the header and footer are omitted from the
     * output by default.
     * 
     * @param toFile
     *            <code>true</code> to write output to a file, <code>false</code>
     *            to write output to a string.
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

    public OptionsBuilder toStream(OutputStream toStream) {
        this.options.setToStream(toStream);
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
        this.options.setToDir(directory.getPath());
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
     * Keeps track of the file and line number for each parsed block.
     * Useful for tooling applications where the association between the converted output and the source file is important.
     *
     * @param sourcemap
     *            value.
     * @return this instance.
     */
    public OptionsBuilder sourcemap(boolean sourcemap) {
        this.options.setSourcemap(sourcemap);
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
     * If true, tells the parser to capture images and links in the reference table.
     * Normally only IDs, footnotes and indexterms are included.
     * The reference table is available via the references property on the document AST object.
     * (Experimental).
     *
     * @param catalogAssets
     *            value.
     * @return this instance.
     */
    public OptionsBuilder catalogAssets(boolean catalogAssets) {
        this.options.setCatalogAssets(catalogAssets);
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
     * If true, the source is parsed eagerly (i.e., as soon as the source is passed to the load or load_file API).
     * If false, parsing is deferred until the parse method is explicitly invoked.
     *
     * @param parse
     *            value.
     * @return this instance.
     */
    public OptionsBuilder parse(boolean parse) {
        this.options.setParse(parse);
        return this;
    }

    /**
     * Sets parse header only falg.
     * 
     * @param parseHeaderOnly
     *            value.
     * @return this instance.
     */
    public OptionsBuilder parseHeaderOnly(boolean parseHeaderOnly) {
        this.options.setParseHeaderOnly(parseHeaderOnly);
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
        this.options.setBaseDir(baseDir.getPath());
        return this;
    }

    /**
     * Returns a valid Options instance.
     *
     * @return options instance.
     */
    public Options build() {
        return this.options;
    }

}
