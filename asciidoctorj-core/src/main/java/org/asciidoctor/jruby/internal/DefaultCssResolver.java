package org.asciidoctor.jruby.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.jruby.Ruby;
import org.jruby.RubyRuntimeAdapter;
import org.jruby.RubyString;

public class DefaultCssResolver {

    private static final String ASCIIDCOTOR_CSS_NAME = "asciidoctor.css";
    private static final String STYLESHEETS_ASCIIDOCTOR_CSS = "stylesheets/" + ASCIIDCOTOR_CSS_NAME;

    private Ruby runtime;
    private RubyRuntimeAdapter evaler;

    private String defaultCssContent = null;

    public DefaultCssResolver(Ruby runtime, RubyRuntimeAdapter evaler) {
        this.runtime = runtime;
        this.evaler = evaler;
    }

    public void treatCopyCssAttribute(File currentDirectory, Map<String, Object> options) {

        String content = loadContent();
        try {
            File destinationDirectory = getDestinationDirectory(currentDirectory, options);
            writeContent(destinationDirectory, content);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public boolean isCopyCssActionRequired(Map<String, Object> options) {

        Map<String, Object> attributes = (Map<String, Object>) options.get(Options.ATTRIBUTES);

        if (attributes != null && isCopyCssPresent(attributes)) {
            // if linkcss is not present by default is considered as true.
            if (isLinkCssWithValidValue(attributes)) {
                if (isStylesheetWithValidValue(attributes)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isStylesheetWithValidValue(Map<String, Object> attributes) {
        return isAttributeNotConfirmed(attributes, Attributes.STYLESHEET_NAME)
                || "".equals(attributes.get(Attributes.STYLESHEET_NAME))
                || "DEFAULT".equals(attributes.get(Attributes.STYLESHEET_NAME));
    }

    private boolean isLinkCssWithValidValue(Map<String, Object> attributes) {
        return isAttributeNotConfirmed(attributes, Attributes.LINK_CSS)
                || "".equals(attributes.get(Attributes.LINK_CSS));
    }

    private boolean isCopyCssPresent(Map<String, Object> attributes) {
        return "".equals(attributes.get(Attributes.COPY_CSS));
    }

    private boolean isAttributeNotConfirmed(Map<String, Object> attributes, String attribute) {
        return (!attributes.containsKey(attribute) && !attributes.containsKey(attribute + "!"));
    }

    private File getDestinationDirectory(File currentDirectory, Map<String, Object> options) {

        if ("".equals(options.get(Options.IN_PLACE))) {
            return currentDirectory;
        } else {

            if (options.containsKey(Options.TO_FILE)) {

                File toFile = new File((String) options.get(Options.TO_FILE));

                if (toFile.isAbsolute()) {
                    return toFile.getParentFile();
                } else {
                    if (options.containsKey(Options.TO_DIR)) {
                        File toDir = new File((String) options.get(Options.TO_DIR));
                        return new File(toDir, toFile.getParent());
                    } else {
                        return new File(currentDirectory, toFile.getParent());
                    }
                }

            } else {

                if (options.containsKey(Options.TO_DIR)) {
                    return new File((String) options.get(Options.TO_DIR));
                } else {
                    return currentDirectory;
                }
            }
        }
    }

    private void writeContent(File directory, String content) throws IOException {
        File fullPath = new File(directory, ASCIIDCOTOR_CSS_NAME);
        IOUtils.writeFull(new FileWriter(fullPath), content);
    }

    private String loadContent() {
        if (defaultCssContent == null) {
            InputStream defaultCssInputStream = DefaultCssResolver.class.getResourceAsStream(cssClasspathLocation());
            defaultCssContent = IOUtils.readFull(defaultCssInputStream);
        }

        return this.defaultCssContent;
    }

    private String cssClasspathLocation() {
        return "/" + gemClasspath() + "/" + STYLESHEETS_ASCIIDOCTOR_CSS;
    }

    private String gemClasspath() {
        RubyString gemLocationRubyObject = (RubyString) evaler.eval(runtime,
                "Gem.loaded_specs['asciidoctor'].full_gem_path");
        String gemLocation = gemLocationRubyObject.asJavaString();

        return gemLocation.substring(gemLocation.indexOf("gems"), gemLocation.length());
    }
}
