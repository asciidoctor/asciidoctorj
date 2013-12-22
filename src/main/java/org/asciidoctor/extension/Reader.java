package org.asciidoctor.extension;

import java.util.List;

public interface Reader {

    List<String> lines();
    
    void advance();
    
}
