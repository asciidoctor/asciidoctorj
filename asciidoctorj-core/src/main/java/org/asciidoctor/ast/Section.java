package org.asciidoctor.ast;

public interface Section extends AbstractBlock {

    int index();
    int number();
    String sectname();
    boolean special();
    int numbered();
}
