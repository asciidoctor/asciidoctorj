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
        return getSymbol("style");
    }

    @Override
    public void setStyle(String style) {
        setSymbol("style", style);
    }

    @Override
    public Table.HAlign getHAlign() {
        return Table.HAlign.valueOf(((String) getAttr("halign", "left")).toUpperCase());
    }

    @Override
    public void setHAlign(Table.HAlign halign) {
        setAttr("halign", halign.name().toLowerCase(), true);
    }

    @Override
    public Table.VAlign getVAlign() {
        return Table.VAlign.valueOf(((String) getAttr("valign", "top")).toUpperCase());
    }

    @Override
    public void setVAlign(Table.VAlign valign) {
        setAttr("valign", valign.name().toLowerCase(), true);
    }

}
