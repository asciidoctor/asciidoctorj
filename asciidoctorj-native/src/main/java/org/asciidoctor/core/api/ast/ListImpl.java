package org.asciidoctor.core.api.ast;

import org.asciidoctor.ast.List;
import org.asciidoctor.ast.StructuralNode;

/**
 * The {@link List} of the AsciidoctorJ API (any list but a description list).
 */
public class ListImpl extends StructuralNodeImpl<org.asciidoctor.core.List> implements List {

    public ListImpl(org.asciidoctor.core.List delegate) {
        super(delegate);
    }

    @Override
    public java.util.List<StructuralNode> getItems() {
        return new NodeList<>(delegate.getItems());
    }

    @Override
    public boolean hasItems() {
        return delegate.hasItems();
    }
}
