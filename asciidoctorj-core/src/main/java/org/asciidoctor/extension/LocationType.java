package org.asciidoctor.extension;

/**
 * Location used by the {@link Location} annotation.
 */
public enum LocationType {

    HEADER(":header"),
    FOOTER(":footer");

    private final String optionValue;

    private LocationType(String optionValue) {
        this.optionValue = optionValue;
    }

    public String optionValue() {
        return optionValue;
    }
}
