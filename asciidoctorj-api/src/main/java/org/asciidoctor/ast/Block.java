package org.asciidoctor.ast;

import java.util.List;

public interface Block extends StructuralNode {

    /**
     * @return The original content of this block
     */
    List<String> getLines();

    /**
     * Sets the source lines of the Block.
     *
     * @param lines The source of this Block, substitutions will still be applied.
     */
    void setLines(List<String> lines);

    /**
     * @return The String containing the lines joined together or null if there are no lines
     */
    String getSource();

    /**
     * Sets the source of the Block.
     *
     * @param source The source of this Block, substitutions will still be applied.
     */
    void setSource(String source);

}
