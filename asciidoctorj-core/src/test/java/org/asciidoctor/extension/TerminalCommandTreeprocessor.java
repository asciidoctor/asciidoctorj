package org.asciidoctor.extension;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalCommandTreeprocessor extends Treeprocessor {

    private Document document;

    public TerminalCommandTreeprocessor() {
    }

    public TerminalCommandTreeprocessor(Map<String, Object> config) {
        super(config);
    }

    @Override
    public Document process(Document document) {
        this.document = document;
        processBlock(this.document);
        return this.document;
    }

    private void processBlock(StructuralNode block) {

        List<StructuralNode> blocks = block.getBlocks();

        for (int i = 0; i < blocks.size(); i++) {
            final StructuralNode currentBlock = blocks.get(i);
            if (currentBlock instanceof StructuralNode) {
                if ("paragraph".equals(currentBlock.getContext())) {
                    List<String> lines = ((Block) currentBlock).getLines();
                    if (lines.size() > 0 && lines.get(0).startsWith("$")) {
                        blocks.set(i, convertToTerminalListing((Block) currentBlock));
                    }
                } else {
                    // It's not a paragraph, so recursively descent into the child node
                    processBlock(currentBlock);
                }
            }
        }
    }

    public Block convertToTerminalListing(Block block) {

        Map<String, Object> attributes = block.getAttributes();
        attributes.put("role", "terminal");
        StringBuilder resultLines = new StringBuilder();

        List<String> lines = block.getLines();

        for (String line : lines) {
            if (line.startsWith("$")) {
                resultLines.append("<span class=\"command\">")
                        .append(line.substring(2, line.length()))
                        .append("</span>");
            } else {
                resultLines.append(line);
            }
        }

        return createBlock(this.document, "listing",
                List.of(resultLines.toString()), attributes, new HashMap<>());
    }

}
