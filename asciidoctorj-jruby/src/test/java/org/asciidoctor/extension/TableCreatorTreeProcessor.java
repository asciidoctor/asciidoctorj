package org.asciidoctor.extension;

import java.util.HashMap;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Table;

public class TableCreatorTreeProcessor extends Treeprocessor {

    @Override
    public Document process(Document document) {

        // Create and add the table
        Table table = createTable(document, new HashMap<>());
        document.getBlocks().add(table);

        // Create and add a column
        Column column = createTableColumn(table, 0, new HashMap<>());
        column.setHorizontalAlignment(Table.HorizontalAlignment.CENTER);
        column.setVerticalAlignment(Table.VerticalAlignment.BOTTOM);
        table.getColumns().add(column);

        // Create and add a row to the table
        Row row = createTableRow(table);
        table.getBody().add(row);

        // Create and add a cell to the row
        Cell cell = createTableCell(column, "A1");
        row.getCells().add(cell);

        // Create and add a row to the table
        Row headerRow = createTableRow(table);
        table.getHeader().add(headerRow);

        // Create and add a cell to the row
        Cell headerCell = createTableCell(column, "A");
        headerRow.getCells().add(headerCell);

        // Create and add a row to the table
        Row footerRow = createTableRow(table);
        table.getFooter().add(footerRow);

        // Create and add a cell to the row
        Cell footerCell = createTableCell(column, "Sum of A");
        footerRow.getCells().add(footerCell);

        return document;
    }
}
