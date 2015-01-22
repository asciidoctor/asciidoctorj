package org.asciidoctor.ast;

public interface Inline extends AbstractNode {

    @Deprecated
    public String render();
    
    public String convert();

    public String getType();

    public String getText();
}
