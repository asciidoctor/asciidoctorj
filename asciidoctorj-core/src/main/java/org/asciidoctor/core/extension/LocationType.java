package org.asciidoctor.core.extension;

/**
 * The location of the content contributed by a docinfo processor.
 */
public enum LocationType {
    HEADER(":head"),
    FOOTER(":footer");

    private final String optionValue;

    LocationType(String optionValue) {
        this.optionValue = optionValue;
    }

    public String optionValue() {
        return optionValue;
    }
}
