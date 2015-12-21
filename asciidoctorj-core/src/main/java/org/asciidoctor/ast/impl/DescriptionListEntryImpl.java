package org.asciidoctor.ast.impl;

import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.internal.RubyBlockListDecorator;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;

public class DescriptionListEntryImpl extends StructuralNodeImpl implements DescriptionListEntry {

    public DescriptionListEntryImpl(IRubyObject listDelegate) {
        super(listDelegate);
    }

    @Override
    public List<ListItem> getTerms() {
        return new RubyBlockListDecorator<ListItem>((RubyArray) getAt(0));
    }

    @Override
    public ListItem getDescription() {
        return (ListItem) NodeConverter.createASTNode(getAt(1));
    }

    private Object getAt(int i) {
        return ((RubyArray) getRubyObject()).get(i);
    }
}
