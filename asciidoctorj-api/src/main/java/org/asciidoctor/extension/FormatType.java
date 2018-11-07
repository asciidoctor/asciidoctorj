package org.asciidoctor.extension;

/**
 * Inline macro format used by the {@link Format} annotation.
 */
public enum FormatType {
    LONG(":long"),
    SHORT(":short"),
    CUSTOM(":regexp");

    private final String optionValue;

    FormatType(String optionValue) {
        this.optionValue = optionValue;
    }

    public String optionValue() {
        return optionValue;
    }
}
