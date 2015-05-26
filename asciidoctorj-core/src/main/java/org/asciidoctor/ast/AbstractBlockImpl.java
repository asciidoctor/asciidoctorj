package org.asciidoctor.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.asciidoctor.converter.ConverterProxy;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyObject;
import org.jruby.javasupport.JavaEmbedUtils;

public class AbstractBlockImpl extends AbstractNodeImpl implements AbstractBlock {

    private static final String BLOCK_CLASS = "Block";
    private static final String SECTION_CLASS = "Section";
    
    protected AbstractBlock delegate;


    public AbstractBlockImpl(AbstractBlock blockDelegate, Ruby runtime) {
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
    public List<AbstractBlock> blocks() {
        return getBlocks();
    }

    @Override
    public List<AbstractBlock> getBlocks() {
        List<AbstractBlock> rubyBlocks = delegate.getBlocks();

        for (int i = 0; i < rubyBlocks.size(); i++) {
            Object abstractBlock = rubyBlocks.get(i);
            if (!(abstractBlock instanceof RubyArray)) {
                rubyBlocks.set(i, (AbstractBlock) NodeConverter.createASTNode(abstractBlock));
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
    public AbstractBlock delegate() {
        return delegate;
    }

    @Override
    public List<AbstractBlock> findBy(Map<Object, Object> selector) {

        @SuppressWarnings("unchecked")
        List<?> findBy = delegate.findBy(RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime,
                selector));

        List<AbstractBlock> ret = new ArrayList<AbstractBlock>(findBy.size());
        for (Object abstractBlock: findBy) {
            if (!(abstractBlock instanceof RubyArray)) {
                ret.add((AbstractBlock) NodeConverter.createASTNode(abstractBlock));
            }

        }
        return ret;
    }

}
