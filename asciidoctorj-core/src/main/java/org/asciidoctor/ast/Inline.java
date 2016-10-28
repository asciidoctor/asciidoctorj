package org.asciidoctor.ast;

public interface Inline extends AbstractNode {

    @Deprecated
    String render();
    
    String convert();

    String getType();

    String getText();
}
