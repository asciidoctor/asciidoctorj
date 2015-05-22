package org.asciidoctor.ast.impl;

import java.util.List;

import org.asciidoctor.ast.Block;
import org.jruby.Ruby;

public class BlockImpl extends BlockNodeImpl implements Block {
    private Block blockDelegate;

    public BlockImpl(Block blockDelegate, Ruby runtime) {
        super(blockDelegate, runtime);
        this.blockDelegate = blockDelegate;
    }

    @Override
    public List<String> lines() {
        return blockDelegate.lines();
    }
    
    @Override
    public String source() {
        return blockDelegate.source();
    }
}
