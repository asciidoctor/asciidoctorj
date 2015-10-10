package org.asciidoctor.ast;

public interface PhraseNode extends ContentNode {

    @Deprecated
    public String render();
    
    public String convert();

    public String getType();

    public String getText();

    public String getTarget();
}
