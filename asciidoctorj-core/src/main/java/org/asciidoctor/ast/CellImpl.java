package org.asciidoctor.ast;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class CellImpl extends AbstractNodeImpl implements Cell {

    private final Cell delegate;

    public CellImpl(Cell delegate, Ruby rubyRuntime) {
        super(delegate, rubyRuntime);
        this.delegate = delegate;
    }

    @Override
    public Column getColumn() {
        return (Column) getParent();
    }

    @Override
    public int getColspan() {
        return delegate.getColspan();
    }

    @Override
    public int getRowspan() {
        return delegate.getRowspan();
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public Object getContent() {
        return delegate.getContent();
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

    @Override
    public DocumentRuby getInnerDocument() {
        return (DocumentRuby) NodeConverter.createASTNode((IRubyObject) delegate.getInnerDocument());
    }

}