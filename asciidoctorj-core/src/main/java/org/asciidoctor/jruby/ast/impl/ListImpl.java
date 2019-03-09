package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.List;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.internal.RubyBlockListDecorator;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

public class ListImpl extends StructuralNodeImpl implements List {

    public ListImpl(IRubyObject delegate) {
        super(delegate);
    }

    @Override
    public java.util.List<StructuralNode> getItems() {
        RubyArray rubyBlocks = (RubyArray) getRubyProperty("items");
        return new RubyBlockListDecorator<>(rubyBlocks);
    }

    @Override
    public boolean hasItems() {
        return getBoolean("items?");
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
    public java.util.List<StructuralNode> getBlocks() {
        RubyArray rubyBlocks = (RubyArray) getRubyProperty("blocks");
        return new RubyBlockListDecorator<>(rubyBlocks);
    }
}
