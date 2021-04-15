package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface Document extends StructuralNode {

    /**
     * @return The Title structure for this document.
     * @see Title
     */
    Title getStructuredDoctitle();

    /**
     * @return The title as a String.
     * @see Title
     */
    String getDoctitle();

    /**
     * @deprecated Please use {@link #getDoctitle()}
     * @return The title as a String.
     * @see Title
     */
    @Deprecated
    String doctitle();

    /**
     * Gets the author(s) information as defined in the author line
     * in the document header, or in author & email attributes.
     *
     * @return authors information
     */
    List<Author> getAuthors();

    /**
     * @return basebackend attribute value
     */
    boolean isBasebackend(String backend);

    /**
     * @deprecated Please use {@link #isBasebackend(String)}
     * @return basebackend attribute value
     */
    @Deprecated
    boolean basebackend(String backend);

    /**
     *
     * @return options defined in document.
     */
    Map<Object, Object> getOptions();

    /**
     * Gets the current counter with the given name and increases its value.
     * At the first invocation the counter will return 1.
     * After the call the value of the counter is set to the returned value plus 1.
     * @param name
     * @return
     */
    int getAndIncrementCounter(String name);

    /**
     * Gets the current counter with the given name and increases its value.
     * At the first invocation the counter will return the given initial value.
     * After the call the value of the counter is set to the returned value plus 1.
     * @param name
     * @param initialValue
     * @return
     */
    int getAndIncrementCounter(String name, int initialValue);

    /**
     * @return Whether the sourcemap is enabled.
     */
    boolean isSourcemap();

    /**
     * Toggles the sourcemap option.
     *
     * This method must be called before the document is parsed, such as
     * from a Preprocessor extension. Otherwise, it has no effect.
     *
     * @param state The state in which to put the sourcemap (true = on, false = off).
     */
    void setSourcemap(boolean state);


    /**
     * The catalog contains data collected by asciidoctor that is useful to a converter.
     *
     * Note that the catalog is not part of the asciidoctor public API and is subject to change.
     *
     * @return catalog
     */
    Catalog getCatalog();


    /**
     * The revision information with: date, number and remark.
     *
     * @return revisionInfo
     */
    RevisionInfo getRevisionInfo();
}
