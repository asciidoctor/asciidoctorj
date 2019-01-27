package org.asciidoctor.ruby.ast.impl;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ruby.internal.RubyBlockListDecorator;
import org.asciidoctor.ruby.internal.RubyObjectWrapper;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;

public class RowImpl extends RubyObjectWrapper implements Row {

    public RowImpl(IRubyObject rubyObject) {
        super(rubyObject);
    }

    @Override
    public List<Cell> getCells() {
        return new RubyBlockListDecorator<Cell>((RubyArray) getRubyObject());
    }

    public RubyArray getRubyCells() {
        return (RubyArray) getRubyObject();
    }

}
