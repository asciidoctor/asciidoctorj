package org.asciidoctor.jruby.internal;

import org.asciidoctor.api.extension.Processor;
import org.asciidoctor.api.extension.ProcessorFactory;
import org.asciidoctor.jruby.extension.internal.JRubyProcessor;

public class ProcessorFactoryImpl implements ProcessorFactory {

    @Override
    public Processor createProcessorDelegate() {
        return new JRubyProcessor();
    }

}
