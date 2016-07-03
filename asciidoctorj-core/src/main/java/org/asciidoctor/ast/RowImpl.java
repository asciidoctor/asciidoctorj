package org.asciidoctor.ast;

import org.jruby.Ruby;

import java.util.List;

public class RowImpl implements Row {

    private final List<Cell> cells;

    private final Ruby rubyRuntime;

    public RowImpl(List<Cell> cells, Ruby rubyRuntime) {
        this.rubyRuntime = rubyRuntime;

        this.cells = cells;
    }

    @Override
    public List<Cell> getCells() {
        return cells;
    }
}