package org.asciidoctor.arquillian;

import org.asciidoctor.Asciidoctor;

public class ScopedAsciidoctor {

    private Asciidoctor sharedAsciidoctor;

    private Asciidoctor unsharedAsciidoctor;

    public void setSharedAsciidoctor(Asciidoctor sharedAsciidoctor) {
        this.sharedAsciidoctor = sharedAsciidoctor;
    }

    public Asciidoctor getSharedAsciidoctor() {
        return sharedAsciidoctor;
    }

    public void setUnsharedAsciidoctor(Asciidoctor unsharedAsciidoctor) {
        this.unsharedAsciidoctor = unsharedAsciidoctor;
    }

    public Asciidoctor getUnsharedAsciidoctor() {
        return unsharedAsciidoctor;
    }

}
