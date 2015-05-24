package org.asciidoctor.ast.impl;

import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.BlockNode;
import org.asciidoctor.ast.Node;
import org.asciidoctor.internal.RubyHashUtil;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyObject;

public class BlockNodeImpl extends NodeImpl implements BlockNode {

    private static final String BLOCK_CLASS = "Block";
    private static final String SECTION_CLASS = "Section";
    
    protected BlockNode delegate;


    public BlockNodeImpl(BlockNode blockDelegate, Ruby runtime) {
        super(blockDelegate, runtime);
        this.delegate = blockDelegate;
    }

    @Override
    public String title() {
        return getTitle();
    }

    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    @Override
    public String style() {
        return getStyle();
    }

    @Override
    public String getStyle() {
        return delegate.getStyle();
    }

    @Override
    public List<BlockNode> blocks() {
        return getBlocks();
    }

    @Override
    public List<BlockNode> getBlocks() {
        List<BlockNode> rubyBlocks = delegate.getBlocks();

        for (int i = 0; i < rubyBlocks.size(); i++) {
            Object abstractBlock = rubyBlocks.get(i);
            if (!(abstractBlock instanceof RubyArray) && !(abstractBlock instanceof Node)) {
                RubyObject rubyObject = (RubyObject) abstractBlock;
                rubyBlocks.set(i, (BlockNode) NodeConverter.createASTNode(rubyObject));
            }
        }

        return rubyBlocks;
    }

    @Override
    public Object content() {
        return getContent();
    }

    @Override
    public Object getContent() {
        return delegate.content();
    }

    @Override
    public String getNodeName() {
        return delegate.getNodeName();
    }

    @Override
    public String convert() {
        return delegate.convert();
    }

    @Override
    public int getLevel() {
        return delegate.getLevel();
    }

    @Override
    public BlockNode delegate() {
        return delegate;
    }

    @Override
    public List<BlockNode> findBy(Map<Object, Object> selector) {

        @SuppressWarnings("unchecked")
        List<BlockNode> findBy = delegate.findBy(RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime,
                selector));

        for (int i = 0; i < findBy.size(); i++) {
            Object abstractBlock = findBy.get(i);
            if (!(abstractBlock instanceof RubyArray) && !(abstractBlock instanceof BlockNode)) {
                RubyObject rubyObject = (RubyObject)abstractBlock;
                findBy.set(i, (BlockNode) NodeConverter.createASTNode(rubyObject));
            }

        }
        return findBy;
    }

}
