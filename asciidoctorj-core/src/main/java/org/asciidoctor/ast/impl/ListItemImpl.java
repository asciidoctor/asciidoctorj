package org.asciidoctor.ast.impl;

import org.asciidoctor.ast.ListItem;
import org.jruby.Ruby;

public class ListItemImpl extends BlockNodeImpl implements ListItem {

    private final ListItem listDelegate;

    public ListItemImpl(ListItem listDelegate, Ruby runtime) {
        super(listDelegate, runtime);
        this.listDelegate = listDelegate;
    }

    @Override
    public String getMarker() {
        return listDelegate.getMarker();
    }

    @Override
    public String getText() {
        return listDelegate.getText();
    }

    @Override
    public boolean hasText() {
        return listDelegate.hasText();
    }
}
