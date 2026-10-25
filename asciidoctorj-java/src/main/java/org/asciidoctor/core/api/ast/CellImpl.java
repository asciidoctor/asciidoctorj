package org.asciidoctor.core.api.ast;

import java.util.Locale;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Table;

/**
 * The {@link Cell} of the AsciidoctorJ API.
 */
public class CellImpl extends StructuralNodeImpl<org.asciidoctor.core.Table.Cell> implements Cell {

    public CellImpl(org.asciidoctor.core.Table.Cell delegate) {
        super(delegate);
    }

    @Override
    public Column getColumn() {
        return NodeConverter.wrap(delegate.getColumn());
    }

    @Override
    public int getColspan() {
        Integer colspan = delegate.getColspan();
        return colspan == null ? 0 : colspan;
    }

    @Override
    public int getRowspan() {
        Integer rowspan = delegate.getRowspan();
        return rowspan == null ? 0 : rowspan;
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public String getSource() {
        return delegate.getSource();
    }

    @Override
    public void setSource(String source) {
        delegate.setText(source);
    }

    /**
     * @return the converted content of the cell: the converted inner document of an AsciiDoc cell, otherwise the List
     * of converted paragraphs
     */
    @Override
    public Object getContent() {
        if ("asciidoc".equals(delegate.getStyle())) {
            return delegate.getContent();
        }
        return delegate.getParagraphs();
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
        return Table.HorizontalAlignment.valueOf(((String) getAttribute("halign", "left")).toUpperCase(Locale.ROOT));
    }

    @Override
    public void setHorizontalAlignment(Table.HorizontalAlignment halign) {
        setAttribute("halign", halign.name().toLowerCase(Locale.ROOT), true);
    }

    @Override
    public Table.VerticalAlignment getVerticalAlignment() {
        return Table.VerticalAlignment.valueOf(((String) getAttribute("valign", "top")).toUpperCase(Locale.ROOT));
    }

    @Override
    public void setVerticalAlignment(Table.VerticalAlignment valign) {
        setAttribute("valign", valign.name().toLowerCase(Locale.ROOT), true);
    }

    @Override
    public Document getInnerDocument() {
        return NodeConverter.wrap(delegate.getInnerDocument());
    }

    @Override
    public void setInnerDocument(Document document) {
        delegate.setInnerDocument(NodeConverter.unwrap(document));
    }
}
