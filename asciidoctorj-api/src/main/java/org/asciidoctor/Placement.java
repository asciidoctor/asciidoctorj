package org.asciidoctor;

public enum Placement {


    TOP("top"), BOTTOM("bottom"), LEFT("left"), RIGHT("right"),
    PREAMBLE("preamble"), MACRO("macro");

    private String position;
    
    Placement(String position) {
        this.position = position;
    }
    
    public String getPosition() {
        return position;
    }
    
}
