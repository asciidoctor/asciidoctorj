package org.asciidoctor.core;

/**
 * A {@link Writer} that does not write output.
 */
public interface VoidWriter<T> extends Writer<T> {

    /**
     * Does not write output.
     */
    @Override
    default void write(T output, Object target) {
    }
}
