package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.api.ast.DescriptionList;
import org.asciidoctor.api.ast.List;
import org.asciidoctor.api.ast.StructuralNode;
import org.asciidoctor.api.ast.ContentNode;
import org.asciidoctor.api.ast.Block;
import org.asciidoctor.api.ast.Document;
import org.asciidoctor.api.ast.ListItem;
import org.asciidoctor.api.ast.Section;
import org.asciidoctor.api.extension.Treeprocessor;

public class ASTExtractorTreeprocessor extends Treeprocessor {

    public static StringBuilder result = new StringBuilder();


    @Override
    public Document process(Document document) {
        processNode(0, document);
        return document;
    }

    public void processNode(int level, ContentNode node) {

        for (int i = 0; i < level; i++) {
            result.append("  ");
        }
        String className = node.getClass().getSimpleName().replaceAll("[iI]mpl(\\.?)", "");
        result.append(className);
        for (int i = level * 2 + className.length(); i < 20; i++) {
            result.append(' ');
        }

        if (node instanceof StructuralNode) {
            StructuralNode block = (StructuralNode) node;
            String context = "";
            if (block.getContext() != null) {
                context = " context: " + block.getContext();
            }
            result.append(context);
            for (int i = context.length(); i < 20; i++) {
                result.append(' ');
            }
            if (block.getStyle() != null) {
                result.append(" style: ").append(block.getStyle());
            }
            if (block instanceof Section) {
                result.append(" level: ").append(block.getLevel());
            }
            if (block instanceof Block) {
                String source = ((Block) block).getSource();
                if (source != null && source.trim().length() > 0) {
                    result.append("\n                    ");
                    if (source.length() > 20) {
                        source = source.substring(0, 19) + "...";
                    }
                    result.append(source);
                }
            }
            if (block instanceof ListItem) {
                String source = ((ListItem) block).getText();
                if (source != null && source.trim().length() > 0) {
                    result.append("\n                    ");
                    if (source.length() > 20) {
                        source = source.substring(0, 19) + "...";
                    }
                    result.append(source);
                }
            }
        }
        result.append("\n");

        if (node instanceof List) {
            for (StructuralNode child: ((List) node).getItems()) {
                processNode(level + 1, child);
            }
        } else if (node instanceof DescriptionList) {
            // Ignore it, we don't showcase description lists.
        } else if (node instanceof StructuralNode) {
            for (StructuralNode child: ((StructuralNode) node).getBlocks()) {
                processNode(level + 1, child);
            }
        }
    }
}
