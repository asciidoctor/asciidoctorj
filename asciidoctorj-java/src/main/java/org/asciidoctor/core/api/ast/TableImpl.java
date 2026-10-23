package org.asciidoctor.core.api.ast;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Table;

/**
 * The {@link Table} of the AsciidoctorJ API.
 */
public class TableImpl extends StructuralNodeImpl<org.asciidoctor.core.Table> implements Table {
    private static final String FRAME_ATTR = "frame";
    private static final String GRID_ATTR = "grid";

    public TableImpl(org.asciidoctor.core.Table delegate) {
        super(delegate);
    }

    @Override
    public boolean hasHeaderOption() {
        return delegate.hasHeaderOption();
    }

    @Override
    public List<Column> getColumns() {
        return new NodeList<>(delegate.getColumns());
    }

    @Override
    public List<Row> getHeader() {
        return new RowList(delegate.getRows().getHead());
    }

    @Override
    public List<Row> getFooter() {
        return new RowList(delegate.getRows().getFoot());
    }

    @Override
    public List<Row> getBody() {
        return new RowList(delegate.getRows().getBody());
    }

    @Override
    public String getFrame() {
        return (String) getAttribute(FRAME_ATTR, "all");
    }

    @Override
    public void setFrame(String frame) {
        setAttribute(FRAME_ATTR, frame, true);
    }

    @Override
    public String getGrid() {
        return (String) getAttribute(GRID_ATTR, "all");
    }

    @Override
    public void setGrid(String grid) {
        setAttribute(GRID_ATTR, grid, true);
    }

    @Override
    public void assignColumnWidths() {
        int widthBase = 0;
        List<org.asciidoctor.core.Table.Column> autowidthCols = new ArrayList<>();
        for (Column column : getColumns()) {
            int width = column.getWidth();
            if (width < 0) {
                autowidthCols.add(NodeConverter.unwrap(column));
            } else {
                widthBase += width;
            }
        }
        delegate.assignColumnWidths(widthBase, autowidthCols.isEmpty() ? null : autowidthCols);
    }

    /**
     * A live view of rows of the table, which keeps the rowcount attribute up to date.
     */
    private final class RowList extends AbstractList<Row> implements RandomAccess {
        private final List<org.asciidoctor.core.Table.Row> rows;

        private RowList(List<org.asciidoctor.core.Table.Row> rows) {
            this.rows = rows;
        }

        @Override
        public Row get(int index) {
            return new RowImpl(rows.get(index));
        }

        @Override
        public int size() {
            return rows.size();
        }

        @Override
        public Row set(int index, Row element) {
            return new RowImpl(rows.set(index, RowImpl.unwrap(element)));
        }

        @Override
        public void add(int index, Row element) {
            rows.add(index, RowImpl.unwrap(element));
            updateRowCount();
        }

        @Override
        public Row remove(int index) {
            Row previous = new RowImpl(rows.remove(index));
            updateRowCount();
            return previous;
        }

        @Override
        public void clear() {
            rows.clear();
            updateRowCount();
        }

        @Override
        public int indexOf(Object o) {
            return o instanceof RowImpl row ? rows.indexOf(row.getDelegate()) : -1;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }

        private void updateRowCount() {
            setAttribute("rowcount", size(), true);
        }
    }
}
