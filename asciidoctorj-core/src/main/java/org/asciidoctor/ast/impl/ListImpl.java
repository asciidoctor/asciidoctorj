package org.asciidoctor.ast.impl;

import org.asciidoctor.ast.List;
import org.asciidoctor.ast.impl.StructuralNodeImpl;
import org.jruby.runtime.builtin.IRubyObject;

public class ListImpl extends StructuralNodeImpl implements List {

    public ListImpl(IRubyObject delegate) {
        super(delegate);
    }

    @Override
    public java.util.List getItems() {
        return getBlocks();
    }

    @Override
    public boolean hasItems() {
        return getBoolean("items?");
    }

    @Override
    public String render() {
        return getString("render");
    }

    @Override
    public String convert() {
        return getString("convert");
    }
}
