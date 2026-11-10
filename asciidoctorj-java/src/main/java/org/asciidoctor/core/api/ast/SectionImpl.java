package org.asciidoctor.core.api.ast;

import org.asciidoctor.ast.Section;

/**
 * The {@link Section} of the AsciidoctorJ API.
 */
public class SectionImpl extends StructuralNodeImpl<org.asciidoctor.core.Section> implements Section {

    public SectionImpl(org.asciidoctor.core.Section delegate) {
        super(delegate);
    }

    @Override
    public int getIndex() {
        return delegate.getIndex();
    }

    @Override
    public String getNumeral() {
        return delegate.getNumeral();
    }

    @Override
    public String getSectionName() {
        return delegate.getSectionName();
    }

    @Override
    public boolean isSpecial() {
        return delegate.isSpecial();
    }

    @Override
    public boolean isNumbered() {
        return delegate.isNumbered();
    }

    @Override
    public String getSectnum() {
        return delegate.getSectnum();
    }

    @Override
    public String getSectnum(String delimiter) {
        return delegate.getSectnum(delimiter);
    }
}
