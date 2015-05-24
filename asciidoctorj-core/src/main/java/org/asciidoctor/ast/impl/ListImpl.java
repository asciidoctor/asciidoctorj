package org.asciidoctor.ast.impl;

import org.asciidoctor.ast.List;
import org.jruby.Ruby;

public class ListImpl extends BlockNodeImpl implements List {

    private final List listDelegate;

    public ListImpl(List delegate, Ruby rubyRuntime) {
        super(delegate, rubyRuntime);
        this.listDelegate = delegate;
    }

    @Override
    public java.util.List getItems() {
        return blocks();
    }

    @Override
    public boolean isItem() {
        return isBlock();
    }

    @Override
    public String render() {
        return listDelegate.render();
    }

    @Override
    public String convert() {
        return listDelegate.convert();
    }
}
