package org.asciidoctor.ast;

public interface List extends BlockNode {

    java.util.List<BlockNode> getItems();

    boolean isItem();

    @Deprecated
    public String render();

    public String convert();

}
