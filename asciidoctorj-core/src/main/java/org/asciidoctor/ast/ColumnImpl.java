package org.asciidoctor.ast;

import org.jruby.runtime.builtin.IRubyObject;

public class ColumnImpl extends AbstractNodeImpl implements Column {

    public ColumnImpl(IRubyObject rubyNode) {
        super(rubyNode);
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
    public Table getTable() {
        return (Table) getParent();
    }

    @Override
    public int getColumnNumber() {
        return Integer.parseInt((String) getAttr("colnumber"));
    }

    @Override
    public int getWidth() {
        return ((Number) getAttr("width")).intValue();
    }

    @Override
    public void setWidth(int width) {
        setAttr("width", width, true);
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
