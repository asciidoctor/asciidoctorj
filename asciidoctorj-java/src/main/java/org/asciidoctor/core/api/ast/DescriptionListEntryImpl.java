package org.asciidoctor.core.api.ast;

import java.util.List;

import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.ListItem;

/**
 * The {@link DescriptionListEntry} of the AsciidoctorJ API.
 */
public class DescriptionListEntryImpl extends StructuralNodeImpl<org.asciidoctor.core.DescriptionListEntry>
        implements DescriptionListEntry {

    public DescriptionListEntryImpl(org.asciidoctor.core.DescriptionListEntry delegate) {
        super(delegate);
    }

    @Override
    public List<ListItem> getTerms() {
        return new NodeList<>(delegate.getTerms());
    }

    @Override
    public ListItem getDescription() {
        return NodeConverter.wrap(delegate.getDescription());
    }

    @Override
    public void setDescription(ListItem description) {
        delegate.setDescription(NodeConverter.unwrap(description));
    }
}
