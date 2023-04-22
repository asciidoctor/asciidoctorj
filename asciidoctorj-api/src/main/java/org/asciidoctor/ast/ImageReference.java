package org.asciidoctor.ast;

/**
 * Image reference view as available in the assets catalog.
 *
 * @since 3.0.0
 */
public interface ImageReference {

    /**
     * The image target describing the image location.
     * The target may be a path or a URL.
     *
     * @return image target
     */
    String getTarget();

    /**
     * Value of the 'imagesdir' attribute applied to resolve the image location.
     *
     * @return imagesdir value
     */
    String getImagesdir();
}
