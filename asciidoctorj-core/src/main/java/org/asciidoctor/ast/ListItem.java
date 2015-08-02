package org.asciidoctor.ast;

public interface ListItem extends StructuralNode {

    public String getMarker();

    public String getText();

    public boolean hasText();
}
