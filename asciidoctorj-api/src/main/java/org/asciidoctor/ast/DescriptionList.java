package org.asciidoctor.ast;

public interface DescriptionList extends StructuralNode {

    java.util.List<DescriptionListEntry> getItems();

    boolean hasItems();

    String convert();

}
