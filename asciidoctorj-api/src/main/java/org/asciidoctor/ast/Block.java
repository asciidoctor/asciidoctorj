package org.asciidoctor.ast;

import java.util.List;

public interface Block extends StructuralNode {
    /**
     * @deprecated Please use {@link #getLines}
     * @return The original content of this block
     */
    @Deprecated
    List<String> lines();

    /**
     * @return The original content of this block
     */
    List<String> getLines();

    /**
     * Sets the source lines of the Block.
     * @param lines The source of this Block, substitutions will still be applied.
     */
    void setLines(List<String> lines);

    /**
     * @deprecated Please use {@link #getSource}
     * @return the String containing the lines joined together or null if there are no lines
     */
    @Deprecated
    String source();

    /**
     * @return the String containing the lines joined together or null if there are no lines
     */
    String getSource();

    /**
     * Sets the source of the Block.
     * @param source The source of this Block, substitutions will still be applied.
     */
    void setSource(String source);

}
