package org.asciidoctor.ast;

public interface DescriptionList extends StructuralNode {

    java.util.List<DescriptionListEntry> getItems();

    boolean hasItems();

    @Deprecated
    String render();

    String convert();

}
