package org.asciidoctor.ast;

import java.util.List;

import org.jruby.runtime.builtin.IRubyObject;

public class BlockImpl extends AbstractBlockImpl implements Block {

    public BlockImpl(IRubyObject blockDelegate) {
        super(blockDelegate);
    }

    @Override
    public List<String> lines() {
        return getLines();
    }

    @Override
    public List<String> getLines() {
        return getList("lines", String.class);
    }

    @Override
    public String source() {
        return getSource();
    }

    @Override
    public String getSource() {
        return getString("source");
    }

}
