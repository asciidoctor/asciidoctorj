package org.asciidoctor.ast;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;

import java.util.List;

public class ListImpl extends AbstractBlockImpl implements ListNode {

    private final ListNode listDelegate;

    public ListImpl(ListNode delegate, Ruby rubyRuntime) {
        super(delegate, rubyRuntime);
        this.listDelegate = delegate;
    }

    @Override
    public List<AbstractBlock> getItems() {
        return blocks();
    }

    @Override
    public boolean hasItems() {
        return isItems();
    }

    /**
     * This method will be invoked by Ruby.
     */
    public boolean isItems() {
        return isBlocks();
    }

    @Override
    public String render() {
        return listDelegate.render();
    }

    @Override
    public String convert() {
        return listDelegate.convert();
    }

    public boolean isOutline() {
        final String context = getContext();
        return "ulist".equals(context) || "olist".equals(context);
    }
}
