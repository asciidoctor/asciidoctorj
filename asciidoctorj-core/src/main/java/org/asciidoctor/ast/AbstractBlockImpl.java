package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyObject;

public class AbstractBlockImpl extends AbstractNodeImpl implements AbstractBlock {

    private static final String BLOCK_CLASS = "Block";
    private static final String SECTION_CLASS = "Section";
    
    protected AbstractBlock delegate;


    public AbstractBlockImpl(AbstractBlock blockDelegate, Ruby runtime) {
        super(blockDelegate, runtime);
        this.delegate = blockDelegate;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public String title() {
        return delegate.title();
    }

    @Override
    public String role() {
        return delegate.role();
    }

    @Override
    public String style() {
        return delegate.style();
    }

    @Override
    public List<AbstractBlock> blocks() {
        List<AbstractBlock> rubyBlocks = delegate.blocks();

        for (int i = 0; i < rubyBlocks.size(); i++) {
            Object abstractBlock = rubyBlocks.get(i);
            if (!(abstractBlock instanceof RubyArray) && !(abstractBlock instanceof Block)) {
                RubyObject rubyObject = (RubyObject) abstractBlock;
                rubyBlocks.set(i, overrideRubyObjectToJavaObject(rubyObject));
            }
        }

        return rubyBlocks;
    }

    @Override
    public Object content() {
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
    public String context() {
        return delegate.context();
    }

    @Override
    public AbstractBlock delegate() {
        return delegate;
    }

    @Override
    public DocumentRuby document() {
        return delegate.document();
    }

    @Override
    public List<AbstractBlock> findBy(Map<Object, Object> selector) {

        @SuppressWarnings("unchecked")
        List<AbstractBlock> findBy = delegate.findBy(RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime,
                selector));

        for (int i = 0; i < findBy.size(); i++) {
            Object abstractBlock = findBy.get(i);
            if (!(abstractBlock instanceof RubyArray) && !(abstractBlock instanceof AbstractBlock)) {
                RubyObject rubyObject = (RubyObject)abstractBlock;
                findBy.set(i, overrideRubyObjectToJavaObject(rubyObject));
            }

        }
        return findBy;
    }

    private AbstractBlock overrideRubyObjectToJavaObject(RubyObject rubyObject) {
        if (BLOCK_CLASS.equals(rubyObject.getMetaClass().getBaseName())) {
            Block blockRuby = RubyUtils.rubyToJava(runtime, rubyObject, Block.class);
            return new BlockImpl(blockRuby, runtime);
        }
        else if (SECTION_CLASS.equals(rubyObject.getMetaClass().getBaseName())) {
            Section blockRuby = RubyUtils.rubyToJava(runtime, rubyObject, Section.class);
            return new SectionImpl(blockRuby, runtime);
        }
        else {
            AbstractBlock blockRuby = RubyUtils.rubyToJava(runtime, rubyObject, AbstractBlock.class);
            return new AbstractBlockImpl(blockRuby, runtime);
        }
    }
    
}
