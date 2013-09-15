package org.asciidoctor.extension;

public class MyPreprocessorExtension {
    
    private Object document;
    
    public MyPreprocessorExtension(Object object) {
        this.document = object;
    }
    
    public Object process(Object reader, Object lines) {
        System.out.println("Hello!!!");
        return reader;
    }
    
}