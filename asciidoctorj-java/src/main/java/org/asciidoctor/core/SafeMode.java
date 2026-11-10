package org.asciidoctor.core;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The security levels that can be enforced while processing a document.
 */
public enum SafeMode {
    /**
     * A safe mode level that disables any of the security features enforced by Asciidoctor (Java is still subject to
     * its own restrictions).
     */
    UNSAFE(0),

    /**
     * A safe mode level that closely parallels safe mode in AsciiDoc. This value prevents access to files which reside
     * outside of the parent directory of the source file and disables any macro other than the include::[] directive.
     */
    SAFE(1),

    /**
     * A safe mode level that disallows the document from setting attributes that would affect the conversion of the
     * document, in addition to all the security features of {@link #SAFE}. For instance, this level forbids changing
     * the backend or source-highlighter using an attribute defined in the source document header. This is the most
     * fundamental level of security for server deployments (hence the name).
     */
    SERVER(10),

    /**
     * A safe mode level that disallows the document from attempting to read files from the file system and including
     * the contents of them into the document, in additional to all the security features of {@link #SERVER}. For
     * instance, this level disallows use of the include::[] directive and the embedding of binary content (data uri),
     * stylesheets and JavaScripts referenced by the document. (Asciidoctor and trusted extensions may still be allowed
     * to embed trusted content into the document).
     *
     * <p>Since Asciidoctor is aiming for wide adoption, this level is the default and is recommended for server
     * deployments.
     */
    SECURE(20);

    private final int level;

    SafeMode(int level) {
        this.level = level;
    }

    /**
     * @return the integer level of this safe mode
     */
    public int getLevel() {
        return level;
    }

    /**
     * Resolves the safe mode for the specified integer level.
     *
     * @param level the integer level
     * @return the safe mode
     * @throws IllegalArgumentException if the level does not match a safe mode
     */
    public static SafeMode safeMode(int level) {
        for (SafeMode mode : values()) {
            if (mode.level == level) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown safe mode level: " + level);
    }

    /**
     * Resolves the integer level for the specified name (case insensitive).
     *
     * @param name the safe mode name (e.g., safe)
     * @return the integer level or {@code null} if the name is not recognized
     */
    public static Integer valueForName(String name) {
        for (SafeMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode.level;
            }
        }
        return null;
    }

    /**
     * Resolves the lowercase name for the specified integer level.
     *
     * @param value the integer level
     * @return the name of the safe mode or {@code null} if the level does not match a known safe mode
     */
    public static String nameForValue(int value) {
        for (SafeMode mode : values()) {
            if (mode.level == value) {
                return mode.name().toLowerCase(Locale.ROOT);
            }
        }
        return null;
    }

    /**
     * @return the names of all the safe modes, sorted by level
     */
    public static List<String> names() {
        return Arrays.stream(values()).map(mode -> mode.name().toLowerCase(Locale.ROOT)).toList();
    }
}
