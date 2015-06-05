package org.asciidoctor.ast;

public interface Cell extends AbstractNode {

    Column getColumn();

    int getColspan();

    int getRowspan();

    String getText();

    Object getContent();

    String getStyle();
}
