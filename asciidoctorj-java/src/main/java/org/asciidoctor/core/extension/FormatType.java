package org.asciidoctor.core.extension;

/**
 * The format of an inline macro.
 */
public enum FormatType {
    /** The macro has a target (e.g., name:target[attrs]). */
    LONG(":long"),
    /** The macro has no target (e.g., name:[attrs]). */
    SHORT(":short"),
    /** The macro is matched by a custom regular expression. */
    CUSTOM(":regexp");

    private final String optionValue;

    FormatType(String optionValue) {
        this.optionValue = optionValue;
    }

    public String optionValue() {
        return optionValue;
    }
}
