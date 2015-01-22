package org.asciidoctor.ast;

public interface ListNode extends AbstractBlock {

    java.util.List<AbstractBlock> getItems();

    boolean isItem();

    @Deprecated
    public String render();

    public String convert();

}
