package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Section;
import org.jruby.runtime.builtin.IRubyObject;

public class SectionImpl extends StructuralNodeImpl implements Section {

    public SectionImpl(IRubyObject blockDelegate) {
        super(blockDelegate);
    }

    @Override
    @Deprecated
    public int index() {
        return getIndex();
    }

    @Override
    public int getIndex() {
        return getInt("index");
    }

    @Override
    @Deprecated
    public int number() {
        return getNumber();
    }

    @Override
    @Deprecated
    public int getNumber() {
        return getInt("number");
    }

    @Override
    public String getNumeral() {
        return getString("numeral");
    }

    @Override
    @Deprecated
    public String sectname() {
        return getSectionName();
    }

    @Override
    public String getSectionName() {
        return getString("sectname");
    }

    @Override
    @Deprecated
    public boolean special() {
        return isSpecial();
    }

    @Override
    public boolean isSpecial() {
        return getBoolean("special");
    }

    @Override
    @Deprecated
    public boolean numbered() {
        return isNumbered();
    }

    @Override
    public boolean isNumbered() {
        return getBoolean("numbered");
    }

}
