package org.asciidoctor.ast;

import java.util.List;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class BlockImpl extends AbstractBlockImpl implements Block {

    public BlockImpl(IRubyObject blockDelegate) {
        super(blockDelegate);
    }

    @Override
    public List<String> lines() {
        return getList("lines", String.class);
    }
    
    @Override
    public String source() {
        return getString("source");
    }
}
