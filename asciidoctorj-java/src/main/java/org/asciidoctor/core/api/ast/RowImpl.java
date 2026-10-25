package org.asciidoctor.core.api.ast;

import java.util.List;
import java.util.Objects;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Row;
import org.asciidoctor.core.Table;

/**
 * The {@link Row} of the AsciidoctorJ API.
 */
public class RowImpl implements Row {
    private final Table.Row delegate;

    public RowImpl(Table.Row delegate) {
        this.delegate = delegate;
    }

    /**
     * @return the row of the implementation
     */
    public Table.Row getDelegate() {
        return delegate;
    }

    static Table.Row unwrap(Row row) {
        return ((RowImpl) row).delegate;
    }

    @Override
    public List<Cell> getCells() {
        return new NodeList<>(delegate);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RowImpl row && row.delegate == delegate;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(System.identityHashCode(delegate));
    }
}
