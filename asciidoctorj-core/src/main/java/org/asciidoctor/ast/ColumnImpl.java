package org.asciidoctor.ast;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class ColumnImpl extends AbstractNodeImpl implements Column {

    private final Column delegate;

    public ColumnImpl(Column delegate, Ruby rubyRuntime) {
        super(delegate, rubyRuntime);
        this.delegate = delegate;
    }

    @Override
    public String getStyle() {
        return delegate.getStyle();
    }

    @Override
    public void setStyle(String style) {
        delegate.setStyle(style);
    }

    @Override
    public Table getTable() {
        return (Table) NodeConverter.createASTNode((IRubyObject) delegate.getParent());
    }

    @Override
    public int getColnumber() {
        return delegate.getColnumber();
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