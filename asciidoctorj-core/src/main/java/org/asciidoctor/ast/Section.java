package org.asciidoctor.ast;

public interface Section extends BlockNode {

    int index();
    int number();
    String sectname();
    boolean special();
    int numbered();
}
