package org.asciidoctor.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.asciidoctor.converter.ConverterProxy;
import org.asciidoctor.internal.RubyBlockListDecorator;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyObject;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class AbstractBlockImpl extends AbstractNodeImpl implements AbstractBlock {

    private static final String BLOCK_CLASS = "Block";
    private static final String SECTION_CLASS = "Section";
    
    public AbstractBlockImpl(IRubyObject blockDelegate) {
        super(blockDelegate);
    }

    @Override
    public String title() {
        return getTitle();
    }

    @Override
    public String getTitle() {
        return getString("title");
    }

    @Override
    public String style() {
        return getStyle();
    }

    @Override
    public String getStyle() {
        return getString("style");
    }

    @Override
    public List<AbstractBlock> blocks() {
        return getBlocks();
    }

    @Override
    public List<AbstractBlock> getBlocks() {
        RubyArray rubyBlocks = (RubyArray) getProperty("blocks");
        return new RubyBlockListDecorator(rubyBlocks);
    }

    @Override
    public Object content() {
        return getContent();
    }

    @Override
    public Object getContent() {
        return JavaEmbedUtils.rubyToJava(getProperty("content"));
    }

    @Override
    public String convert() {
        return getString("convert");
    }

    @Override
    public int getLevel() {
        return getInt("level");
    }

    @Override
    public List<AbstractBlock> findBy(Map<Object, Object> selector) {

        RubyArray rubyBlocks = (RubyArray) getProperty("find_by", RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime,
                selector));
        return new RubyBlockListDecorator(rubyBlocks);
    }

}
