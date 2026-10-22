package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Link;

public class LinkImpl implements Link {

    private final String text;

    private LinkImpl(String text) {
        this.text = text;
    }

    static Link getInstance(String value) {
        return new LinkImpl(value);
    }

    @Override
    public String getText() {
        return text;
    }
}
