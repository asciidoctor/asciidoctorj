package org.asciidoctor.ast;

public interface PhraseNode extends ContentNode {

    @Deprecated
    String render();
    
    String convert();

    String getType();

    String getText();

    String getTarget();
}
