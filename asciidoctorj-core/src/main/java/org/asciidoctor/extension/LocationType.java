package org.asciidoctor.extension;

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
