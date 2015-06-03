package org.asciidoctor.ast;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class ListItemImpl extends AbstractBlockImpl implements ListItem {

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
    public boolean hasText() {
        return getBoolean("text?");
    }
}
