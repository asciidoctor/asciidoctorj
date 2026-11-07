package org.asciidoctor.core;

import java.util.Locale;

/**
 * Describes the type of content a block accepts and how it should be converted.
 *
 * <p>The values {@link #SKIP}, {@link #ATTRIBUTES} and {@link #TEXT} are only relevant when configuring extensions
 * (i.e., they describe how the content of a custom block or macro should be parsed).
 */
public enum ContentModel {
    /** This block contains other blocks. */
    COMPOUND,
    /** This block holds a paragraph of prose that receives normal substitutions. */
    SIMPLE,
    /** This block holds verbatim text (displayed "as is") that receives verbatim substitutions. */
    VERBATIM,
    /** This block holds unprocessed content passed directly to the output with no substitutions applied. */
    RAW,
    /** This block has no content. */
    EMPTY,
    /** The content of this block is skipped (extensions only). */
    SKIP,
    /** The content of the macro is parsed as attributes (extensions only). */
    ATTRIBUTES,
    /** The content of the macro is treated as text (extensions only). */
    TEXT;

    /**
     * Resolves the content model from the name (e.g., compound) or ContentModel value.
     */
    public static ContentModel of(Object value) {
        if (value == null || value instanceof ContentModel) {
            return (ContentModel) value;
        }
        // NOTE a leading colon is permitted (e.g., :simple), for compatibility with AsciidoctorJ
        return valueOf(value.toString().replaceFirst("^:", "").toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
