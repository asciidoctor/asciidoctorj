package org.asciidoctor.core.api.ast;

import java.util.Locale;

import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Table;

/**
 * The {@link Column} of the AsciidoctorJ API.
 */
public class ColumnImpl extends ContentNodeImpl<org.asciidoctor.core.Table.Column> implements Column {

    public ColumnImpl(org.asciidoctor.core.Table.Column delegate) {
        super(delegate);
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
        return NodeConverter.wrap(delegate.getTable());
    }

    @Override
    public int getColumnNumber() {
        Number columnNumber = (Number) getAttribute("colnumber");
        return columnNumber == null ? -1 : columnNumber.intValue();
    }

    @Override
    public int getWidth() {
        Number width = (Number) getAttribute("width");
        return width == null ? 0 : width.intValue();
    }

    @Override
    public void setWidth(int width) {
        setAttribute("width", width, true);
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
}
