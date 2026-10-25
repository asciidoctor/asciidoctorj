package org.asciidoctor.core.api.ast;

import java.util.Collections;
import java.util.List;

import org.asciidoctor.ast.DescriptionList;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.StructuralNode;

/**
 * The {@link DescriptionList} of the AsciidoctorJ API.
 */
public class DescriptionListImpl extends StructuralNodeImpl<org.asciidoctor.core.List> implements DescriptionList {

    public DescriptionListImpl(org.asciidoctor.core.List delegate) {
        super(delegate);
    }

    @Override
    public List<DescriptionListEntry> getItems() {
        return new NodeList<>(delegate.getItems());
    }

    @Override
    public boolean hasItems() {
        return delegate.hasItems();
    }

    /**
     * @return an empty list, since the items of a description list are not structural nodes (see {@link #getItems()})
     */
    @Override
    public List<StructuralNode> getBlocks() {
        return Collections.emptyList();
    }
}
