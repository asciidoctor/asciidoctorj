package org.asciidoctor.ast;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;

import java.util.List;

public class SectionImpl extends AbstractBlockImpl implements Section {

    private Section delegate;
    
    public SectionImpl(Section blockDelegate, Ruby runtime) {
        super(blockDelegate, runtime);
        this.delegate = blockDelegate;
    }

    @Override
    public int index() {
        return this.delegate.index();
    }

    @Override
    public int number() {
        return this.delegate.number();
    }

    @Override
    public String sectname() {
        return this.delegate.sectname();
    }

    @Override
    public boolean special() {
        return this.delegate.special();
    }

    @Override
    public int numbered() {
        return this.delegate.number();
    }

    public String sectnum() {
        return RubyUtils.invokeRubyMethod(delegate, "sectnum", new Object[0], String.class);
    }

    public String sectnum(String delimiter) {
        return RubyUtils.invokeRubyMethod(delegate, "sectnum", new Object[]{delimiter}, String.class);
    }

    public String sectnum(String delimiter, boolean append) {
        return RubyUtils.invokeRubyMethod(delegate, "sectnum", new Object[]{delimiter, append}, String.class);
    }

    public List<Section> getSections() {
        return RubyUtils.invokeRubyMethod(delegate, "sections", new Object[0], List.class);
    }

    public boolean isSections() {
        return RubyUtils.invokeRubyMethod(delegate, "sections?", new Object[0], Boolean.class);
    }

    public String getCaptionedTitle() {
        return RubyUtils.invokeRubyMethod(delegate, "captioned_title", new Object[0], String.class);
    }
}
