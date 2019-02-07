package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Table;
import org.asciidoctor.jruby.internal.RubyBlockListDecorator;
import org.asciidoctor.jruby.internal.RubyObjectWrapper;
import org.jruby.RubyArray;
import org.jruby.RubyNil;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

public class TableImpl extends StructuralNodeImpl implements Table {

    private static final String FRAME_ATTR = "frame";

    private static final String GRID_ATTR = "grid";

    private Rows rows;

    public TableImpl(IRubyObject rubyObject) {
        super(rubyObject);
        rows = new Rows(getRubyProperty("rows"));
    }

    @Override
    public boolean hasHeaderOption() {
        return getBoolean("has_header_option");
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
    public List<Column> getColumns() {
        RubyArray rubyBlocks = (RubyArray) getRubyProperty("columns");
        return new RubyBlockListDecorator<>(rubyBlocks);
    }

    @Override
    public List<Row> getFooter() {
        return rows.getFooter();
    }

    @Override
    public List<Row> getBody() {
        return rows.getBody();
    }

    @Override
    public List<Row> getHeader() {
        return rows.getHeader();
    }

    private class Rows extends RubyObjectWrapper {

        public Rows(IRubyObject rubyNode) {
            super(rubyNode);
        }

        private RowList getHeader() {
            RubyArray headerRows = (RubyArray) getRubyProperty("head");
            return new RowList(headerRows);
        }

        private RowList getBody() {
            RubyArray bodyRows = (RubyArray) getRubyProperty("body");
            return new RowList(bodyRows);
        }

        private RowList getFooter() {
            RubyArray footerRows = (RubyArray) getRubyProperty("foot");
            return new RowList(footerRows);
        }

        private void setFooterRow(Row row) {
            RubyArray newFooterRows = getRuntime().newArray((IRubyObject) ((RowImpl) row).getRubyCells());
            getRubyObject().getInstanceVariables().setInstanceVariable("@foot", newFooterRows);
        }

    }


    class RowList extends AbstractList<Row> {

        private final RubyArray rubyArray;

        private RowList(RubyArray rubyArray) {
            this.rubyArray = rubyArray;
        }

        @Override
        public int size() {
            return rubyArray.size();
        }

        @Override
        public boolean isEmpty() {
            return rubyArray.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            if (!RowImpl.class.isInstance(o)) {
                return false;
            }
            return rubyArray.contains(((RowImpl) o).getRubyObject());
        }

        @Override
        public boolean add(Row row) {
            boolean changed = rubyArray.add(((RowImpl) row).getRubyObject());
            TableImpl.this.setAttribute("rowcount", size(), true);
            return changed;
        }

        @Override
        public boolean remove(Object o) {
            if (!RowImpl.class.isInstance(o)) {
                return false;
            }
            boolean changed = rubyArray.remove(((RowImpl) o).getRubyObject());
            TableImpl.this.setAttribute("rowcount", size(), true);
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            rubyArray.clear();
            TableImpl.this.setAttribute("rowcount", size(), true);
        }

        @Override
        public Row get(int index) {
            IRubyObject o = rubyArray.at(rubyArray.getRuntime().newFixnum(index));
            if (o == null || o instanceof RubyNil) {
                return null;
            }
            return new RowImpl(o);
        }

        @Override
        public Row set(int index, Row element) {
            Row oldRow = get(index);
            rubyArray.set(index, ((RowImpl) element).getRubyObject());
            return oldRow;
        }

        @Override
        public void add(int index, Row element) {
            rubyArray.add(index, ((RowImpl) element).getRubyObject());
            TableImpl.this.setAttribute("rowcount", size(), true);
        }

        @Override
        public Row remove(int index) {
            IRubyObject rubyObject = (IRubyObject) rubyArray.remove(index);
            TableImpl.this.setAttribute("rowcount", size(), true);
            return new RowImpl(rubyObject);
        }

        @Override
        public int indexOf(Object o) {
            if (!RowImpl.class.isInstance(o)) {
                return -1;
            }
            return rubyArray.indexOf(((RowImpl) o).getRubyObject());
        }

        @Override
        public int lastIndexOf(Object o) {
            if (!RowImpl.class.isInstance(o)) {
                return -1;
            }
            return rubyArray.lastIndexOf(((RowImpl) o).getRubyObject());
        }
    }
}
