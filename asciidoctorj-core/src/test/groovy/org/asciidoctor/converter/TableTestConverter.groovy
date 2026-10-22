package org.asciidoctor.converter

import org.asciidoctor.ast.ContentNode
import org.asciidoctor.ast.Cell
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.Section
import org.asciidoctor.ast.Table

class TableTestConverter extends StringConverter {

    public static final int COLWIDTH = 15
    public static final String COL_SEPARATOR = '|'
    public static final String NEWLINE = '\n'
    public static final String NEWLINE_2 = NEWLINE * 2

    TableTestConverter(String backend, Map<String, Object> opts) {
        super(backend, opts)
    }

    @Override
    String convert(ContentNode node, String transform, Map<Object, Object> opts) {
        if (transform == null) {
            transform = node.nodeName
        }

        this."convert${transform.capitalize()}"(node)
    }

    Object convertEmbedded(Document node) {
        String result = node.doctitle.toUpperCase()
        String content = node.content
        if (content && content.trim().length() > 0) {
            result += NEWLINE_2 + content
        }
        result
    }

    Object convertSection(Section section) {
        String result = "-- ${section.title} --"
        String content = section.content
        if (content && content.trim().length() > 0) {
            result += NEWLINE_2 + content
        }
        result
    }

    Object convertTable(Table table) {

        Closure cellFormatter = { Cell cell ->
            if (cell.style == 'asciidoc') {
                cell.innerDocument.convert().padRight(COLWIDTH)
            } else {
                switch (cell.column.horizontalAlignment) {
                    case Table.HorizontalAlignment.LEFT:
                        cell.text.padRight(COLWIDTH)
                        break
                    case Table.HorizontalAlignment.CENTER:
                        cell.text.center(COLWIDTH)
                        break
                    case Table.HorizontalAlignment.RIGHT:
                        cell.text.padLeft(COLWIDTH)
                        break
                }
            }
        }


        (1..table.columns.size()).each {
            assert table.columns[it - 1].columnNumber == it
        }

        def tableRows = []

        if (table.header.size() > 0) {
            tableRows << table.header[0].cells.collect(cellFormatter).join(COL_SEPARATOR)
            tableRows << table.header[0].cells.collect { '-' * COLWIDTH }.join('+')
        }

        tableRows << table.body.collect { row -> row.cells.collect(cellFormatter).join(COL_SEPARATOR) }

        if (table.footer.size() > 0) {
            tableRows << table.footer[0].cells.collect { '=' * COLWIDTH }.join('#')
            tableRows << table.footer[0].cells.collect(cellFormatter).join(COL_SEPARATOR)
        }

        tableRows.flatten().join(NEWLINE)
    }


}
