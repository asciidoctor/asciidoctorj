package org.asciidoctor.ast;

import java.util.List;

public interface Block extends StructuralNode {
    /**
     * @deprecated Please use {@link #getLines}
     * @return The original content of this block
     */
    List<String> lines();

    /**
     * @return The original content of this block
     */
    List<String> getLines();

    /**
     * @deprecated Please use {@link #getSource}
     * @return the String containing the lines joined together or null if there are no lines
     */
    String source();

    /**
     * @return the String containing the lines joined together or null if there are no lines
     */
    String getSource();

}
