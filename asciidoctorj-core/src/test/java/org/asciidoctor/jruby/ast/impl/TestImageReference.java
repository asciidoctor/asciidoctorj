package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.ImageReference;

/**
 * Test implementation of {@link ImageReference}.
 * Cannot use default implementation because it's package protected.
 */
public class TestImageReference implements ImageReference {

    private final String target;
    private final String imagesdir;

    public TestImageReference(String target) {
        this.target = target;
        this.imagesdir = null;
    }

    public TestImageReference(String target, String imagesdir) {
        this.target = target;
        this.imagesdir = imagesdir;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public String getImagesdir() {
        return imagesdir;
    }
}
