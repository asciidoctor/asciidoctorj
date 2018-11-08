package org.asciidoctor.extension;

/**
 * Location used by the {@link Location} annotation.
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
