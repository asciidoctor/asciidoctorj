package org.asciidoctor.core.api.ast;

import org.asciidoctor.ast.ListItem;

/**
 * The {@link ListItem} of the AsciidoctorJ API.
 */
public class ListItemImpl extends StructuralNodeImpl<org.asciidoctor.core.ListItem> implements ListItem {

    public ListItemImpl(org.asciidoctor.core.ListItem delegate) {
        super(delegate);
    }

    @Override
    public String getMarker() {
        return delegate.getMarker();
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public String getSource() {
        return delegate.getSource();
    }

    @Override
    public void setSource(String source) {
        delegate.setText(source);
    }

    @Override
    public boolean hasText() {
        return delegate.hasText();
    }
}
