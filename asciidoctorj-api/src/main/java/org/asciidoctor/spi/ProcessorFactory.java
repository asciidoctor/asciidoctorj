package org.asciidoctor.spi;

import org.asciidoctor.extension.Processor;

public interface ProcessorFactory {

    Processor createProcessorDelegate();

}
