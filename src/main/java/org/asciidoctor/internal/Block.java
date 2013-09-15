package org.asciidoctor.internal;

import java.util.List;
import java.util.Map;

public interface Block {

    String context();
    List<String> lines();
    Map<String, Object> attributes();
    
    
}
