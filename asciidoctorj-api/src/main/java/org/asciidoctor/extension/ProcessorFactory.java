package org.asciidoctor.extension;

/**
 * An implementation of the AsciidoctorJ API has to implement this interface.
 * Every instance of a Processor will call this method to get a delegate of the {@link Processor}
 * interface that has to implement functionalities like creating AST nodes etc.
 */
public interface ProcessorFactory {

    Processor createProcessorDelegate();

}
