package org.asciidoctor.ast.impl;

import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.impl.StructuralNodeImpl;
import org.jruby.runtime.builtin.IRubyObject;

public class SectionImpl extends StructuralNodeImpl implements Section {

    public SectionImpl(IRubyObject blockDelegate) {
        super(blockDelegate);
    }

    @Override
    public int index() {
        return getIndex();
    }

    @Override
    public int getIndex() {
        return getInt("index");
    }

    @Override
    public int number() {
        return getNumber();
    }

    @Override
    public int getNumber() {
        return getInt("number");
    }

    @Override
    public String sectname() {
        return getSectionName();
    }

    @Override
    public String getSectionName() {
        return getString("sectname");
    }

    @Override
    public boolean special() {
        return isSpecial();
    }

    @Override
    public boolean isSpecial() {
        return getBoolean("special");
    }

    @Override
    public boolean numbered() {
        return isNumbered();
    }

    @Override
    public boolean isNumbered() {
        return getBoolean("numbered");
    }

}
