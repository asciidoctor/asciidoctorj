package org.asciidoctor.ast;

public interface ListNode extends AbstractBlock {

    java.util.List<AbstractBlock> getItems();

    boolean hasItems();

    @Deprecated
    String render();

    String convert();

}
