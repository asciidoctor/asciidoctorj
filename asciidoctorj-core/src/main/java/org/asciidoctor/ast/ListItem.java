package org.asciidoctor.ast;

public interface ListItem extends AbstractBlock {

    String getMarker();

    String getText();

    boolean hasText();
}
