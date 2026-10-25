package org.asciidoctor.core.api.ast;

import java.util.Arrays;
import java.util.List;

import org.asciidoctor.ast.Block;

/**
 * The {@link Block} of the AsciidoctorJ API.
 */
public class BlockImpl extends StructuralNodeImpl<org.asciidoctor.core.Block> implements Block {

    public BlockImpl(org.asciidoctor.core.Block delegate) {
        super(delegate);
    }

    @Override
    public List<String> getLines() {
        return delegate.getLines();
    }

    @Override
    public void setLines(List<String> lines) {
        delegate.setLines(lines);
    }

    @Override
    public String getSource() {
        return delegate.getSource();
    }

    @Override
    public void setSource(String source) {
        setLines(Arrays.asList(source.split("\n")));
    }
}
