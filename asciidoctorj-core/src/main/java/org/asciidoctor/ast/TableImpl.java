package org.asciidoctor.ast;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.ArrayList;
import java.util.List;

public class TableImpl extends AbstractBlockImpl implements Table {

    private static final String FRAME_ATTR = "frame";

    private static final String GRID_ATTR  = "grid";

    private Table delegate;

    private Rows rows;

    public TableImpl(Table delegate, Ruby rubyRuntime) {
        super(delegate, rubyRuntime);
        this.delegate = delegate;

        IRubyObject rubyTable = JavaEmbedUtils.javaToRuby(rubyRuntime, delegate);
        rows = new RowsImpl(
            RubyUtils.rubyToJava(rubyRuntime, rubyTable.getInstanceVariables().getInstanceVariable("@rows"), Rows.class),
            rubyRuntime);
    }

    @Override
    public boolean hasHeaderOption() {
        return delegate.hasHeaderOption();
    }

    @Override
    public String getFrame() {
        return (String)getAttr(FRAME_ATTR, "all");
    }

    @Override
    public void setFrame(String frame) {
        setAttr(FRAME_ATTR, frame, true);
    }

    @Override
    public String getGrid() {
        return (String)getAttr(GRID_ATTR, "all");
    }

    @Override
    public void setGrid(String grid) {
        setAttr(GRID_ATTR, grid, true);
    }

    @Override
    public List<Column> getColumns() {
        List<Column> rubyColumns = delegate.getColumns();
        List<Column> result = new ArrayList<Column>(rubyColumns.size());

        for (Column column : rubyColumns) {
            IRubyObject rubyColumn = (IRubyObject) column;
            result.add((Column) NodeConverter.createASTNode(rubyColumn));
        }

        return result;
    }

    @Override
    public List<Row> getFooter() {
        return rows.getFoot();
    }

    @Override
    public List<Row> getBody() {
        return rows.getBody();
    }

    @Override
    public List<Row> getHeader() {
        return rows.getHead();
    }

    public interface Rows {
        List<Row> getHead();

        List<Row> getFoot();

        List<Row> getBody();
    }

    public static class RowsImpl implements Rows {

        private final Rows delegate;

        private final Ruby rubyRuntime;

        public RowsImpl(Rows delegate, Ruby rubyRuntime) {
            this.delegate = delegate;
            this.rubyRuntime = rubyRuntime;
        }

        public List<Row> getHead() {
            return convertRows(delegate.getHead());
        }

        public List<Row> getFoot() {
            return convertRows(delegate.getFoot());
        }

        public List<Row> getBody() {
            return convertRows(delegate.getBody());
        }

        private List<Row> convertRows(List<?> rows) {
            List<Row> result = new ArrayList<Row>(rows.size());
            for (Object row : rows) {
                List<? extends IRubyObject> rubyRow = (List<? extends IRubyObject>) row;
                result.add(convertRow(rubyRow));
            }
            return result;
        }

        private Row convertRow(List<? extends IRubyObject> rubyRow) {
            List<Cell> cells = new ArrayList<Cell>(rubyRow.size());
            for (IRubyObject rubyCell : rubyRow) {
                cells.add((Cell) NodeConverter.createASTNode(rubyCell));
            }
            return new RowImpl(cells, rubyRuntime);
        }

    }

}