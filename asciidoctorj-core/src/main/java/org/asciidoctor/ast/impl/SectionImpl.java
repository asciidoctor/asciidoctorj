package org.asciidoctor.ast.impl;

import org.asciidoctor.ast.Section;
import org.jruby.Ruby;

public class SectionImpl extends BlockNodeImpl implements Section {

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
