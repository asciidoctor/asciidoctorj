package org.asciidoctor.extension;

public enum FormatType {
    LONG(":long"),
    SHORT(":short"),
    CUSTOM(":regexp");

    private final String optionValue;

    private FormatType(String optionValue) {
        this.optionValue = optionValue;
    }

    public String optionValue() {
        return optionValue;
    }
}
