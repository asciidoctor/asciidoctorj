package org.asciidoctor.ast;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;

public class ListItemImpl extends AbstractBlockImpl implements ListItem {

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
        return isText();
    }

    public boolean isText() {
        return RubyUtils.invokeRubyMethod(delegate, "text?", new Object[0], Boolean.class);
    }

    public boolean isSimple() {
        return RubyUtils.invokeRubyMethod(delegate, "simple?", new Object[0], Boolean.class);
    }

    public boolean isCompound() {
        return RubyUtils.invokeRubyMethod(delegate, "compound?", new Object[0], Boolean.class);
    }
}
