package org.asciidoctor.internal;

import java.util.List;

public interface Reader {

    List<String> lines();
    
    void advance();
    
}
