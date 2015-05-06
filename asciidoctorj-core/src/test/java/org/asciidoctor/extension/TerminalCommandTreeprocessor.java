package org.asciidoctor.extension;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.DocumentRuby;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalCommandTreeprocessor extends Treeprocessor {

	private DocumentRuby document;
	
    public TerminalCommandTreeprocessor(Map<String, Object> config) {
        super(config);
    }

    @Override
    public DocumentRuby process(DocumentRuby document) {
        this.document = document;
        processBlock((AbstractBlock) this.document);
        return this.document;
    }

    private void processBlock(AbstractBlock block) {

        List<AbstractBlock> blocks = block.getBlocks();

        for (int i = 0; i < blocks.size(); i++) {
            final AbstractBlock currentBlock = blocks.get(i);
            if(currentBlock instanceof AbstractBlock) {
                if ("paragraph".equals(currentBlock.getContext())) {
                    List<String> lines = ((Block) currentBlock).lines();
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

        List<String> lines = block.lines();

        for (String line : lines) {
            if (line.startsWith("$")) {
                resultLines.append("<span class=\"command\">")
                        .append(line.substring(2, line.length()))
                        .append("</command");
            } else {
                resultLines.append(line);
            }
        }

        return createBlock(this.document, "listing", Arrays.asList(resultLines.toString()), attributes,
                new HashMap<Object, Object>());
    }

}
