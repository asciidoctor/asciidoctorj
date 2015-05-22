package org.asciidoctor.ast.impl;

import org.asciidoctor.ast.Inline;
import org.jruby.Ruby;

public class InlineImpl extends NodeImpl implements Inline {

    protected Inline delegate;

    public InlineImpl(Inline delegate, Ruby ruby) {
        super(delegate, ruby);
        this.delegate = delegate;
    }

    @Override
    public String render() {
        return delegate.render();
    }

    @Override
    public String convert() {
        return delegate.convert();
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public String getTarget() {
        return delegate.getTarget();
    }
}
