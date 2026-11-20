package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.jruby.internal.RubyBlockListDecorator;
import org.asciidoctor.jruby.internal.RubyObjectWrapper;
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
        Object firstItem = getAt(1);
        return firstItem == null ? null : (ListItem) NodeConverter.createASTNode(firstItem);
    }

    public void setDescription(final ListItem description) {
        setAt(1, description);
    }

    private Object getAt(int i) {
        return ((RubyArray) getRubyObject()).get(i);
    }

    private void setAt(int i, Object object) {
        ((RubyArray) getRubyObject()).set(i, RubyObjectWrapper.class.cast(object).getRubyObject());
    }


}
