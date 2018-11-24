package org.asciidoctor.ast;

public interface List extends StructuralNode {

    java.util.List<StructuralNode> getItems();

    boolean hasItems();

    /**
     * @deprecated Please use {@link #convert()}
     * @return
     */
    @Deprecated
    String render();

    String convert();

}
