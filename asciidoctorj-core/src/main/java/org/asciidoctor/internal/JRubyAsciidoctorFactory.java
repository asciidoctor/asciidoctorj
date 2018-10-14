package org.asciidoctor.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.impl.ProcessorNodeFactoryImpl;
import org.asciidoctor.extension.spi.AsciidoctorFactory;
import org.asciidoctor.extension.spi.ProcessorNodeFactory;

import java.util.List;

public class JRubyAsciidoctorFactory implements AsciidoctorFactory {

    private ProcessorNodeFactory processorNodeFactory = new ProcessorNodeFactoryImpl();

    @Override
    public Asciidoctor create() {
        return JRubyAsciidoctor.create();
    }

    @Override
    public Asciidoctor create(String gemPath) {
        return JRubyAsciidoctor.create(gemPath);
    }

    @Override
    public Asciidoctor create(List<String> loadPaths) {
        return JRubyAsciidoctor.create(loadPaths);
    }

    @Override
    public Asciidoctor create(ClassLoader classloader) {
        return JRubyAsciidoctor.create(classloader);
    }

    @Override
    public Asciidoctor create(ClassLoader classloader, String gemPath) {
        return JRubyAsciidoctor.create(classloader, gemPath);
    }

    @Override
    public Asciidoctor create(List<String> loadPaths, String gemPath) {
        return JRubyAsciidoctor.create(loadPaths, gemPath);
    }

    @Override
    public ProcessorNodeFactory getProcessorNodeFactory() {
        return processorNodeFactory;
    }


}

