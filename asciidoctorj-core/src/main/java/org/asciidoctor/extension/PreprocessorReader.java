package org.asciidoctor.extension;

import java.util.Map;

import org.asciidoctor.ast.Document;

public interface PreprocessorReader extends Reader {

    void push_include(String data, String file, String path, int lineNumber, Map<String, Object> attributes);
    
    Document document();
    
}
