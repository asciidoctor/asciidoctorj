package org.asciidoctor.ast.impl;

import org.asciidoctor.api.ast.DescriptionList;
import org.asciidoctor.api.ast.DescriptionListEntry;
import org.asciidoctor.api.ast.StructuralNode;
import org.asciidoctor.internal.RubyBlockListDecorator;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;

public class DescriptionListImpl extends StructuralNodeImpl implements DescriptionList {

    public DescriptionListImpl(IRubyObject delegate) {
        super(delegate);
    }

    @Override
    public java.util.List<DescriptionListEntry> getItems() {
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
    public List<StructuralNode> getBlocks() {
        return null;
    }
}
