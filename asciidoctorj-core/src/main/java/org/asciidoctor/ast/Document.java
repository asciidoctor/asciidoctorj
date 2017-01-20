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
    String doctitle();

    /**
     * 
     * @return page title
     */
    String title();

    /**
     * 
     * @return attributes defined in document
     */
    Map<String, Object> getAttributes();

    /**
     * @return basebackend attribute value
     */
    boolean isBasebackend(String backend);

    /**
     * @deprecated Please use {@link #isBasebackend(String)}
     * @return basebackend attribute value
     */
    boolean basebackend(String backend);

    /**
     *
     * @return blocks contained within current Document.
     */
    List<StructuralNode> blocks();

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
}
