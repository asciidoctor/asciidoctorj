package org.asciidoctor;

public enum CompatMode {

    DEFAULT("default"), LEGACY("legacy");
    
    private String mode;
    
    private CompatMode(String mode) {
        this.mode = mode;
    }
    
    public String getMode() {
        return mode;
    }
    
}
