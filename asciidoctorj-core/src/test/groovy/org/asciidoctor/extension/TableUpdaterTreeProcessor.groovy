package org.asciidoctor.extension

import groovy.transform.CompileStatic
import org.asciidoctor.api.ast.Cell
import org.asciidoctor.api.ast.Document
import org.asciidoctor.api.ast.StructuralNode
import org.asciidoctor.api.ast.Table
import org.asciidoctor.api.extension.Treeprocessor

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
