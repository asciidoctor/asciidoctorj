package org.asciidoctor.ast;

public interface Inline {

    @Deprecated
    public String render();
    
    public String convert();
    
}
