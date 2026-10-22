package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.ast.Cell
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.ast.Table

@CompileStatic
class TableUpdaterTreeProcessor extends Treeprocessor {

    @Override
    Document process(Document document) {
        processNode(document)
        document
    }

    static void processNode(StructuralNode node) {
        if (node instanceof Table) {
            processTable(node)
        } else {
            node.blocks.each { processNode it }
        }
    }

    static void processTable(Table table) {
        def cells = table.body.collectMany { it.cells } as List<Cell>
        cells.each { cell ->
            cell.source = '*Replaced*'
        }
    }
}
