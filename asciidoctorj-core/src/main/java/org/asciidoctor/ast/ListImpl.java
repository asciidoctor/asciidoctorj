package org.asciidoctor.ast;

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
    public boolean isItem() {
        return isBlock();
    }

    @Override
    public String render() {
        return listDelegate.render();
    }

    @Override
    public String convert() {
        return listDelegate.convert();
    }
}
