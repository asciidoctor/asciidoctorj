package org.asciidoctor.ast;

public interface Inline extends Node {

    @Deprecated
    public String render();
    
    public String convert();

    public String getType();

    public String getText();

    public String getTarget();
}
