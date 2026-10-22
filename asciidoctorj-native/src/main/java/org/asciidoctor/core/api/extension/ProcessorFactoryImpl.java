package org.asciidoctor.core.api.extension;

import org.asciidoctor.extension.Processor;
import org.asciidoctor.extension.ProcessorFactory;

/**
 * Creates the {@link ProcessorImpl} to which the processors of the AsciidoctorJ API delegate (registered as a service).
 */
public class ProcessorFactoryImpl implements ProcessorFactory {

    @Override
    public Processor createProcessorDelegate() {
        return new ProcessorImpl();
    }
}
