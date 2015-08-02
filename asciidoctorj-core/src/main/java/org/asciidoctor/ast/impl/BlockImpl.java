package org.asciidoctor.ast.impl;

import java.util.List;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.impl.StructuralNodeImpl;
import org.jruby.runtime.builtin.IRubyObject;

public class BlockImpl extends StructuralNodeImpl implements Block {

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
