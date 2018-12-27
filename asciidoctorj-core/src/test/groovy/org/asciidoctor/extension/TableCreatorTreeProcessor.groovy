package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.api.ast.Cell
import org.asciidoctor.api.ast.Column
import org.asciidoctor.api.ast.Document
import org.asciidoctor.api.ast.Row
import org.asciidoctor.api.ast.Table
import org.asciidoctor.api.extension.Treeprocessor

@CompileStatic
class TableCreatorTreeProcessor extends Treeprocessor {

    @Override
    Document process(Document document) {

        // Create and add the table
        Table table = createTable(document, [:])
        document.blocks.add(table)

        // Create and add a column
        Column column = createTableColumn(table, 0, [:])
        column.setHorizontalAlignment(Table.HorizontalAlignment.CENTER)
        column.setVerticalAlignment(Table.VerticalAlignment.BOTTOM)
        table.columns.add(column)

        // Create and add a row to the table
        Row row = createTableRow(table)
        table.body.add(row)

        // Create and add a cell to the row
        Cell cell = createTableCell(column, 'A1')
        row.cells.add(cell)

        // Create and add a row to the table
        Row headerRow = createTableRow(table)
        table.header << headerRow

        // Create and add a cell to the row
        Cell headerCell = createTableCell(column, 'A')
        headerRow.cells.add(headerCell)

        // Create and add a row to the table
        Row footerRow = createTableRow(table)
        table.footer << footerRow

        // Create and add a cell to the row
        Cell footerCell = createTableCell(column, 'Sum of A')
        footerRow.cells.add(footerCell)

        document
    }
}
