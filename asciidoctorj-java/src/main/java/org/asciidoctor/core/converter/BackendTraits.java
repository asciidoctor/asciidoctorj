package org.asciidoctor.core.converter;

import org.asciidoctor.core.Asciidoctor;
import org.asciidoctor.core.Rx;

/**
 * The traits of a backend (basebackend, filetype, outfilesuffix, htmlsyntax) reported by a {@link Converter}.
 */
public interface BackendTraits {

    /**
     * The values of the backend traits.
     *
     * @param basebackend the base backend (e.g., html)
     * @param filetype the file type (e.g., html)
     * @param htmlsyntax the HTML syntax (html or xml) or null if not applicable
     * @param outfilesuffix the suffix of the output file (e.g., .html)
     */
    record Values(String basebackend, String filetype, String htmlsyntax, String outfilesuffix) {
    }

    String getBasebackend();

    String getFiletype();

    String getHtmlsyntax();

    String getOutfilesuffix();

    /**
     * @return whether this converter supports templates
     */
    default boolean isSupportsTemplates() {
        return false;
    }

    /**
     * Derive backend traits (basebackend, filetype, outfilesuffix, htmlsyntax) from the given backend.
     *
     * @param backend the String backend from which to derive the traits
     * @param basebackend the String basebackend to use in favor of deriving one from the backend (optional, default:
     *            null)
     * @return the backend traits for the given backend.
     */
    static Values derive(String backend, String basebackend) {
        if (backend == null) {
            return new Values(null, null, null, null);
        }
        if (basebackend == null) {
            basebackend = Rx.sub(Rx.TRAILING_DIGITS_RX, backend, "");
        }
        String outfilesuffix = Asciidoctor.DEFAULT_EXTENSIONS.get(basebackend);
        String filetype;
        if (outfilesuffix != null) {
            filetype = outfilesuffix.substring(1);
        } else {
            filetype = basebackend;
            outfilesuffix = "." + filetype;
        }
        return filetype.equals("html") ? new Values(basebackend, filetype, "html", outfilesuffix)
                : new Values(basebackend, filetype, null, outfilesuffix);
    }
}
