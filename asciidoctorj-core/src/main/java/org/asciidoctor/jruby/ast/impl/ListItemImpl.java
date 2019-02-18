package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.ListItem;
import org.jruby.runtime.builtin.IRubyObject;

public class ListItemImpl extends StructuralNodeImpl implements ListItem {

    public ListItemImpl(IRubyObject listDelegate) {
        super(listDelegate);
    }

    @Override
    public String getMarker() {
        return getString("marker");
    }

    @Override
    public String getText() {
        return getString("text");
    }

    @Override
    public String getSource() {
        return getString("@text");
    }

    @Override
    public void setSource(String source) {
        setString("@text", source);
    }

    @Override
    public boolean hasText() {
        return getBoolean("text?");
    }
}
