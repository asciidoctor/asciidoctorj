package org.asciidoctor.extension;

import java.util.List;

public interface Reader {

	List<String> readLines();
    List<String> lines();
    
    void advance();
    
}
