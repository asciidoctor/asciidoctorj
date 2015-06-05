package org.asciidoctor.ast;

import org.jruby.runtime.builtin.IRubyObject;

public class CellImpl extends AbstractNodeImpl implements Cell {

    public CellImpl(IRubyObject rubyNode) {
        super(rubyNode);
    }

    @Override
    public Column getColumn() {
        return (Column) getParent();
    }

    @Override
    public int getColspan() {
        return getInt("colspan");
    }

    @Override
    public int getRowspan() {
        return getInt("rowspan");
    }

    @Override
    public String getText() {
        return getString("text");
    }

    @Override
    public Object getContent() {
        return toJava(getRubyProperty("content"));
    }

    @Override
    public String getStyle() {
        return getString("style");
    }
}
