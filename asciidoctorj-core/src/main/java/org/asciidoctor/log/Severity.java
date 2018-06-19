package org.asciidoctor.log;

public enum Severity {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3),
    FATAL(4),
    UNKNOWN(5);

    private int rubyId;

    private Severity(final int rubyId) {
        this.rubyId = rubyId;
    }

    public int getRubyId() {
        return rubyId;
    }
}
