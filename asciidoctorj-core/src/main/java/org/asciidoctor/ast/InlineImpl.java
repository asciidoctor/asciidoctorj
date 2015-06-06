package org.asciidoctor.ast;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class InlineImpl extends AbstractNodeImpl implements Inline  {

    public InlineImpl(IRubyObject delegate) {
        super(delegate);
    }

    @Override
    public String render() {
        return getString("render");
    }

    @Override
    public String convert() {
        return getString("convert");
    }

    @Override
    public String getType() {
        return getString("type");
    }

    @Override
    public String getText() {
        return getString("text");
    }

    @Override
    public String getTarget() {
        return getString("target");
    }
}
