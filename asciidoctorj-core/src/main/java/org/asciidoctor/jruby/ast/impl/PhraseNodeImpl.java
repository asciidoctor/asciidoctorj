package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.PhraseNode;
import org.jruby.runtime.builtin.IRubyObject;

public class PhraseNodeImpl extends ContentNodeImpl implements PhraseNode {

    public PhraseNodeImpl(IRubyObject delegate) {
        super(delegate);
    }

    @Override
    @Deprecated
    public String render() {
        return getString("render");
    }

    @Override
    public String convert() {
        return getString("convert");
    }

    @Override
    public String getType() {
        return getString("type");
    }

    @Override
    public String getText() {
        return getString("text");
    }

    @Override
    public String getTarget() {
        return getString("target");
    }
}
