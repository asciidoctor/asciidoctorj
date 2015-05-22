package org.asciidoctor.ast;

public interface ListItem extends BlockNode {

    public String getMarker();

    public String getText();

    public boolean hasText();
}
