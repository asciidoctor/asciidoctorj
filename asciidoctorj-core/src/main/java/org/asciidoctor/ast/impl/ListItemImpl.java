package org.asciidoctor.ast.impl;

import org.asciidoctor.api.ast.ListItem;
import org.asciidoctor.ast.impl.StructuralNodeImpl;
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
