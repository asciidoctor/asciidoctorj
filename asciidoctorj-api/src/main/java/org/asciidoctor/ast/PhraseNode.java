package org.asciidoctor.ast;

public interface PhraseNode extends ContentNode {

    /**
     * @deprecated Please use {@link #convert()}
     */
    @Deprecated
    String render();
    
    String convert();

    String getType();

    String getText();

    String getTarget();
}
