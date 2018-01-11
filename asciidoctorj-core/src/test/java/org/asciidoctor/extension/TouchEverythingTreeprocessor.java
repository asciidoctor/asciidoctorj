package org.asciidoctor.extension;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import java.util.Map;

/**
 * This Treeprocessor doesn't do anything useful but only touch every node in the
 * AST so that the whole tree contains nothing but Java AST nodes instead of the Ruby originals.
 * This should reveal misalignments in the between the Ruby AST classes and their Java counterparts.
 */
public class TouchEverythingTreeprocessor extends Treeprocessor {

    public TouchEverythingTreeprocessor(Map<String, Object> config) {
        super(config);
    }

    @Override
    public Document process(Document document) {

        touch(document);

        return document;
    }

    public void touch(StructuralNode block) {

        if (block.getBlocks() != null) {
            for (StructuralNode abstractBlock : block.getBlocks()) {
                touch(abstractBlock);
            }
        }
    }
}
