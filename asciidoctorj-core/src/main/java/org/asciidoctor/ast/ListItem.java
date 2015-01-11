package org.asciidoctor.ast;

public interface ListItem extends AbstractBlock {

    public String getMarker();

    public String getText();

    public boolean hasText();
}
