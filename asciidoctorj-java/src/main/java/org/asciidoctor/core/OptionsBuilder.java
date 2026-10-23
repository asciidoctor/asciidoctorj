package org.asciidoctor.core;

import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Builds an {@link Options} object.
 */
public class OptionsBuilder {
    private final Options options = new Options();

    OptionsBuilder() {
    }

    public OptionsBuilder backend(String backend) {
        options.setBackend(backend);
        return this;
    }

    public OptionsBuilder docType(String docType) {
        options.setDocType(docType);
        return this;
    }

    public OptionsBuilder inPlace(boolean inPlace) {
        options.setInPlace(inPlace);
        return this;
    }

    public OptionsBuilder standalone(boolean standalone) {
        options.setStandalone(standalone);
        return this;
    }

    public OptionsBuilder templateDirs(File... templateDirs) {
        options.setTemplateDirs(Arrays.stream(templateDirs).map(File::getAbsolutePath).toArray(String[]::new));
        return this;
    }

    public OptionsBuilder templateEngine(String templateEngine) {
        options.setTemplateEngine(templateEngine);
        return this;
    }

    public OptionsBuilder templateCache(boolean templateCache) {
        options.setTemplateCache(templateCache);
        return this;
    }

    public OptionsBuilder attributes(Attributes attributes) {
        options.setAttributes(attributes);
        return this;
    }

    public OptionsBuilder toFile(boolean toFile) {
        options.setToFile(toFile);
        return this;
    }

    public OptionsBuilder toFile(File toFile) {
        options.setToFile(toFile.getPath());
        return this;
    }

    public OptionsBuilder toStream(OutputStream toStream) {
        options.setToStream(toStream);
        return this;
    }

    public OptionsBuilder safe(SafeMode safeMode) {
        options.setSafe(safeMode);
        return this;
    }

    public OptionsBuilder toDir(File directory) {
        options.setToDir(directory.getAbsolutePath());
        return this;
    }

    public OptionsBuilder mkDirs(boolean mkDirs) {
        options.setMkDirs(mkDirs);
        return this;
    }

    public OptionsBuilder eruby(String eruby) {
        options.setEruby(eruby);
        return this;
    }

    public OptionsBuilder compact(boolean compact) {
        options.setCompact(compact);
        return this;
    }

    public OptionsBuilder baseDir(File baseDir) {
        options.setBaseDir(baseDir.getAbsolutePath());
        return this;
    }

    public OptionsBuilder sourcemap(boolean sourcemap) {
        options.setSourcemap(sourcemap);
        return this;
    }

    public OptionsBuilder catalogAssets(boolean catalogAssets) {
        options.setCatalogAssets(catalogAssets);
        return this;
    }

    public OptionsBuilder parse(boolean parse) {
        options.setParse(parse);
        return this;
    }

    public OptionsBuilder parseHeaderOnly(boolean parseHeaderOnly) {
        options.setParseHeaderOnly(parseHeaderOnly);
        return this;
    }

    public OptionsBuilder option(String option, Object value) {
        options.setOption(option, value);
        return this;
    }

    public Options build() {
        return options;
    }
}
