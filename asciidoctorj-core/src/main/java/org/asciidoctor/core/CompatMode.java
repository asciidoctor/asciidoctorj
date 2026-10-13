package org.asciidoctor.core;

/**
 * The compatibility mode (the value of the compat-mode attribute).
 */
public enum CompatMode {
    DEFAULT("default"), LEGACY("legacy");

    private final String mode;

    CompatMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
