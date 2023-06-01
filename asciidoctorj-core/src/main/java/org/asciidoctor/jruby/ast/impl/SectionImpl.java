package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Section;
import org.jruby.runtime.builtin.IRubyObject;

public class SectionImpl extends StructuralNodeImpl implements Section {

    public SectionImpl(IRubyObject blockDelegate) {
        super(blockDelegate);
    }

    @Override
    public int getIndex() {
        return getInt("index");
    }

    @Override
    public String getNumeral() {
        return getString("numeral");
    }

    @Override
    public String getSectionName() {
        return getString("sectname");
    }

    @Override
    public boolean isSpecial() {
        return getBoolean("special");
    }

    @Override
    public boolean isNumbered() {
        // Not always an actual boolean, but coercing when object and non-null is enough:
        // https://github.com/asciidoctor/asciidoctorj/issues/1122
        return getBoolean("numbered");
    }

    @Override
    public String getSectnum() {
        return getString("sectnum");
    }

    @Override
    public String getSectnum(String delimiter) {
        return getString("sectnum", new Object[]{delimiter});
    }

}
