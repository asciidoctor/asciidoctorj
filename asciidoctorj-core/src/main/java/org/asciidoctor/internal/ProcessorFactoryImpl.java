package org.asciidoctor.internal;

import org.asciidoctor.extension.JRubyProcessor;
import org.asciidoctor.extension.Processor;
import org.asciidoctor.spi.ProcessorFactory;

public class ProcessorFactoryImpl implements ProcessorFactory {

    @Override
    public Processor createProcessorDelegate() {
        return new JRubyProcessor();
    }

}
