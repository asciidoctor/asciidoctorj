package org.asciidoctor.core;

/**
 * The placement of the table of contents (the value of the toc attribute).
 */
public enum Placement {
    TOP("top"), BOTTOM("bottom"), LEFT("left"), RIGHT("right"), PREAMBLE("preamble"), MACRO("macro");

    private final String position;

    Placement(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }
}
