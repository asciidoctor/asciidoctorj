package org.asciidoctor.ast;

import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;

public class ListImpl extends AbstractBlockImpl implements ListNode {

    public ListImpl(IRubyObject delegate) {
        super(delegate);
    }

    @Override
    public List<AbstractBlock> getItems() {
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
