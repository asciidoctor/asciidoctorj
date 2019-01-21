package org.asciidoctor.ast;

public interface DescriptionList extends StructuralNode {

    java.util.List<DescriptionListEntry> getItems();

    boolean hasItems();

    /**
     * @deprecated Please use {@link #convert()}
     * @return
     */
    @Deprecated
    String render();

    String convert();

}
