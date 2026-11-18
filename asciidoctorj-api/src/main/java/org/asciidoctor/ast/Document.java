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
     * Gets the author(s) information as defined in the author line
     * in the document header, or in author &amp; email attributes.
     *
     * @return The authors information
     * @see Author
     */
    List<Author> getAuthors();

    /**
     * Make the raw source for the Document available.
     * Trailing white characters (spaces, line breaks, etc.) are removed.
     *
     * @return Raw content as String
     */
    String getSource();

    /**
     * Make the raw source lines for the Document available.
     *
     * @return Raw content as List<String>
     */
    List<String> getSourceLines();

    /**
     * @return 'basebackend' attribute value
     */
    boolean isBasebackend(String backend);

    /**
     * @return Options defined in the document
     */
    Map<Object, Object> getOptions();

    /**
     * Gets the current counter with the given name and increases its value.
     * At the first invocation the counter will return 1.
     * After the call the value of the counter is set to the returned value plus 1.
     *
     * @param name name of the counter
     * @return Value before increment plus 1
     */
    int getAndIncrementCounter(String name);

    /**
     * Gets the current counter with the given name and increases its value.
     * At the first invocation the counter will return the given initial value.
     * After the call the value of the counter is set to the returned value plus 1.
     *
     * @param name         name of the counter
     * @param initialValue value to start counter from
     * @return Value before increment plus 1
     */
    int getAndIncrementCounter(String name, int initialValue);

    /**
     * @return Whether the sourcemap is enabled
     */
    boolean isSourcemap();

    /**
     * Determine if the attribute has been set or unset, either in the document (e.g., {@code :!name:} in the
     * header) or in the options (API, CLI), including in the parent document of a nested document.
     *
     * <p>Unlike {@link #hasAttribute(Object)}, this method makes it possible to tell an attribute that has never
     * been specified (so a default can be applied) from an attribute that has been explicitly unset.
     *
     * @param name The name of the attribute
     * @return true if the attribute has been set or unset (explicitly or by an option), false if it has never been
     *         specified (or if the name is empty)
     * @throws NullPointerException if the name is null
     * @since 4.0
     */
    boolean isAttributeSpecified(String name);

    /**
     * Toggles the sourcemap option.
     * <p>
     * This method must be called before the document is parsed, such as
     * from a Preprocessor extension. Otherwise, it has no effect.
     *
     * @param state State in which to put the sourcemap (true = on, false = off)
     */
    void setSourcemap(boolean state);


    /**
     * The catalog contains data collected by asciidoctor that is useful to a converter.
     *
     * @return Catalog assets
     * @see Catalog
     */
    Catalog getCatalog();


    /**
     * The revision information with: date, number and remark.
     *
     * @return revisionInfo
     * @see RevisionInfo
     */
    RevisionInfo getRevisionInfo();
}
