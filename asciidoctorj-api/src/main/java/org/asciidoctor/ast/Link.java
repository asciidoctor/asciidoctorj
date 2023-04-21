package org.asciidoctor.ast;

/**
 * Link view as available in the assets catalog.
 *
 * @since 3.0.0
 */
public interface Link {

    /**
     * The resolved path of the link.
     * The text may be a file path or a URL.
     *
     * @return The link path including substitutions being applied.
     */
    String getText();
}
