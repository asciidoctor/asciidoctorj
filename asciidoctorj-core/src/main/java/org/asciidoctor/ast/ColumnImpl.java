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
        Number columnNumber = (Number) getAttr("colnumber");
        return columnNumber == null ? -1 : columnNumber.intValue();
    }

    @Override
    public int getWidth() {
        Number width =  (Number) getAttr("width");
        return width == null ? 0 : width.intValue();
    }

    @Override
    public void setWidth(int width) {
        setAttr("width", width, true);
    }

    @Override
    public Table.HorizontalAlignment getHorizontalAlignment() {
        return Table.HorizontalAlignment.valueOf(((String) getAttr("halign", "left")).toUpperCase());
    }

    @Override
    public void setHorizontalAlignment(Table.HorizontalAlignment halign) {
        setAttr("halign", halign.name().toLowerCase(), true);
    }

    @Override
    public Table.VerticalAlignment getVerticalAlignment() {
        return Table.VerticalAlignment.valueOf(((String) getAttr("valign", "top")).toUpperCase());
    }

    @Override
    public void setVerticalAlignment(Table.VerticalAlignment valign) {
        setAttr("valign", valign.name().toLowerCase(), true);
    }

}
