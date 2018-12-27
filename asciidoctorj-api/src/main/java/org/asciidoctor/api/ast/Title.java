package org.asciidoctor.api.ast;

public interface Title {

    String getMain();
    String getSubtitle();
    String getCombined();
    boolean isSanitized();
    
}
