package org.asciidoctor.ast;

import org.jruby.Ruby;

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

}
