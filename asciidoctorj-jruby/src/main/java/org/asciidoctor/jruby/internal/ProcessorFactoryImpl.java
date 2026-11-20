package org.asciidoctor.jruby.internal;

import org.asciidoctor.extension.Processor;
import org.asciidoctor.jruby.extension.internal.JRubyProcessor;
import org.asciidoctor.extension.ProcessorFactory;

public class ProcessorFactoryImpl implements ProcessorFactory {

    @Override
    public Processor createProcessorDelegate() {
        return new JRubyProcessor();
    }

}
