package org.asciidoctor.ast;

public interface List extends StructuralNode {

    java.util.List<StructuralNode> getItems();

    boolean hasItems();

    @Deprecated
    public String render();

    public String convert();

}
