package org.asciidoctor.ast;

public interface Cell extends ContentNode {

    Column getColumn();

    int getColspan();

    int getRowspan();

    /**
     * @return The text of the cell including substitutions being applied.
     */
    String getText();

    /**
     * @return The text of the cell without substitutions being applied.
     */
    String getSource();

    /**
     * Sets the source of the Cell.
     * @param source The source of this Cell, substitutions will still be applied.
     */
    void setSource(String source);

    Object getContent();

    /**
     * Returns the style of this cell.
     * The default is {@code null}.
     * Possible values are:
     * <ul>
     *     <li>{@code null}</li>
     *     <li>{@code "strong"}</li>
     *     <li>{@code "emphasis"}</li>
     *     <li>{@code "monospaced"}</li>
     *     <li>{@code "header"}</li>
     *     <li>{@code "literal"}</li>
     *     <li>{@code "verse"}</li>
     *     <li>{@code "asciidoc"}</li>
     * </ul>
     * @return The style of this cell.
     */
    String getStyle();

    /**
     * Sets the style of this cell.
     * @see #getStyle()
     * @param style Values like {@code asciidoc}, {@code verse}, {@code literal}or {@code header}.
     */
    void setStyle(String style);

    /**
     * Returns the horizonzal alignment of this cell.
     * @return a constant representing the horizontal alignment.
     */
    Table.HorizontalAlignment getHorizontalAlignment();

    /**
     * Sets the horizontal alignment of this cell.
     * @param halign Either {@link Table.HorizontalAlignment#LEFT}, {@link Table.HorizontalAlignment#CENTER} or {@link Table.HorizontalAlignment#RIGHT}
     */
    void setHorizontalAlignment(Table.HorizontalAlignment halign);

    /**
     * Returns the vertical alignment of this cell.
     * @return a constant representing the vertical alignment.
     */
    Table.VerticalAlignment getVerticalAlignment();

    /**
     * Sets the vertical alignment of this cell.
     * @param valign Either {@link Table.VerticalAlignment#TOP}, {@link Table.VerticalAlignment#MIDDLE} or {@link Table.VerticalAlignment#BOTTOM}
     */
    void setVerticalAlignment(Table.VerticalAlignment valign);

    /**
     * If the style of a cell is {@code asciidoc} the content of the cell is an inner document.
     * This method returns this inner document.
     * @return The inner document if the cell style is {@code asciidoc}
     */
    Document getInnerDocument();

    /**
     * @see #getInnerDocument()
     */
    void setInnerDocument(Document document);

}
