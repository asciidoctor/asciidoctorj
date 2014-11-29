package org.asciidoctor;

public enum Placement {

    
    TOP("top"), BOTTOM("bottom"), LEFT("left"), RIGHT("right");
    
    private String position;
    
    private Placement(String position) {
        this.position = position;
    }
    
    public String getPosition() {
        return position;
    }
    
}
