package org.asciidoctor.ast;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class SectionImpl extends AbstractBlockImpl implements Section {

    public SectionImpl(IRubyObject blockDelegate) {
        super(blockDelegate);
    }

    @Override
    public int index() {
        return getInt("index");
    }

    @Override
    public int number() {
        return getInt("number");
    }

    @Override
    public String sectname() {
        return getString("sectname");
    }

    @Override
    public boolean special() {
        return getBoolean("special");
    }

    @Override
    public int numbered() {
        return getInt("numbered");
    }

}
