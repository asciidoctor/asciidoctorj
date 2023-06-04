package org.asciidoctor.ast;

public interface PhraseNode extends ContentNode {

    String convert();

    String getType();

    String getText();

    String getTarget();
}
