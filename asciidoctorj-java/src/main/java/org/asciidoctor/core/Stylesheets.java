package org.asciidoctor.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * A utility class for working with the built-in stylesheets.
 */
// QUESTION create methods for link_*_stylesheet?
// QUESTION create method for user stylesheet?
public final class Stylesheets {
    public static final String DEFAULT_STYLESHEET_NAME = "asciidoctor.css";

    /** The classpath location of the built-in stylesheets. */
    public static final String STYLESHEETS_DIR = "/org/asciidoctor/data/stylesheets";

    private static final Stylesheets INSTANCE = new Stylesheets();

    private String primaryStylesheetData;

    private Stylesheets() {
    }

    public static Stylesheets getInstance() {
        return INSTANCE;
    }

    public String getPrimaryStylesheetName() {
        return DEFAULT_STYLESHEET_NAME;
    }

    /**
     * Read the contents of the default Asciidoctor stylesheet.
     *
     * @return the String Asciidoctor stylesheet data
     */
    public synchronized String getPrimaryStylesheetData() {
        if (primaryStylesheetData == null) {
            primaryStylesheetData = Strings.rstrip(readResource(STYLESHEETS_DIR + "/asciidoctor-default.css"));
        }
        return primaryStylesheetData;
    }

    /**
     * Generate code to embed the primary stylesheet.
     *
     * @return the String primary stylesheet data wrapped in a &lt;style&gt; tag
     * @deprecated embed the data returned by {@link #getPrimaryStylesheetData()} instead
     */
    @Deprecated
    public String embedPrimaryStylesheet() {
        return "<style>\n" + getPrimaryStylesheetData() + "\n</style>";
    }

    public void writePrimaryStylesheet(String targetDir) {
        Helpers.writeFile(Helpers.joinPath(targetDir == null ? "." : targetDir, getPrimaryStylesheetName()),
                getPrimaryStylesheetData());
    }

    /**
     * Reads the classpath resource as a UTF-8 String.
     */
    static String readResource(String path) {
        try (InputStream in = Stylesheets.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new UncheckedIOException(new IOException("resource not found: " + path));
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
