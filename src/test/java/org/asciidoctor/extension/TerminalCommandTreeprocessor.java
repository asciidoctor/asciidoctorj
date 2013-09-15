package org.asciidoctor.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.internal.Block;
import org.asciidoctor.internal.DocumentRuby;

public class TerminalCommandTreeprocessor extends Treeprocessor {

    public TerminalCommandTreeprocessor(DocumentRuby documentRuby) {
        super(documentRuby);
    }

    @Override
    public void process() {

        final List<Block> blocks = document.blocks();

        for (int i = 0; i < blocks.size(); i++) {
            final Block currentBlock = blocks.get(i);
            List<String> lines = currentBlock.lines();
            if (lines.size() > 0 && lines.get(0).startsWith("$")) {
                blocks.set(
                        i, convertToTerminalListing(currentBlock));
                        
            }

        }
    }

    public Block convertToTerminalListing(Block block) {

        Map<String, Object> attributes = block.attributes();
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

        return createBlock(document, "listing", resultLines.toString(), attributes,
                new HashMap<String, Object>());
    }

}
