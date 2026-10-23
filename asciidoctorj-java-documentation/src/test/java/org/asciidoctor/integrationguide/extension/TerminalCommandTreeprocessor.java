package org.asciidoctor.integrationguide.extension;

//tag::include[]
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalCommandTreeprocessor extends Treeprocessor {    // <1>

    public TerminalCommandTreeprocessor() {}

    @Override
    public Document process(Document document) {
        processBlock((StructuralNode) document);                     // <2>
        return document;
    }

    private void processBlock(StructuralNode block) {

        List<StructuralNode> blocks = block.getBlocks();

        for (int i = 0; i < blocks.size(); i++) {
            final StructuralNode currentBlock = blocks.get(i);
            if(currentBlock instanceof StructuralNode) {
                if ("paragraph".equals(currentBlock.getContext())) { // <3>
                    List<String> lines = ((Block) currentBlock).getLines();
                    if (lines != null
                            && lines.size() > 0
                            && lines.get(0).startsWith("$")) {
                        blocks.set(i, convertToTerminalListing((Block) currentBlock));
                    }
                } else {
                    // It's not a paragraph, so recursively descend into the child node
                    processBlock(currentBlock);
                }
            }
        }
    }
    public Block convertToTerminalListing(Block originalBlock) {
        Map<Object, Object> options = new HashMap<Object, Object>();
        options.put("subs", ":specialcharacters");

        Block block = createBlock(                                   // <4>
                (StructuralNode) originalBlock.getParent(),
                "listing",
                originalBlock.getLines(),
                originalBlock.getAttributes(),
                options);

        block.addRole("terminal");                                   // <5>
        return block;
    }
}
//end::include[]
