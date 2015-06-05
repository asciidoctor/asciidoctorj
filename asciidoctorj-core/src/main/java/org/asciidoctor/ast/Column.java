package org.asciidoctor.ast;

public interface Column extends AbstractNode {

    /**
     * @return the Symbol style for this column.
     */
    String getStyle();

    Table getTable();

    int getColumnNumber();

    int getWidth();

    String getHAlign();

    String getVAlign();

}
