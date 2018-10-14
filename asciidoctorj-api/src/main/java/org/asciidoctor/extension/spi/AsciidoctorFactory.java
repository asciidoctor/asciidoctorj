package org.asciidoctor.extension.spi;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.asciidoctor.Asciidoctor;

public interface AsciidoctorFactory {

    static class Instance {
        static AsciidoctorFactory factory;
    }

    static AsciidoctorFactory getFactory() {
        if (Instance.factory == null) {
            Iterator<AsciidoctorFactory> iterator = ServiceLoader.load(AsciidoctorFactory.class).iterator();
            if (!iterator.hasNext()) {
                iterator = ServiceLoader.load(AsciidoctorFactory.class).iterator();
                if (!iterator.hasNext()) {
                    throw new IllegalStateException("Could not find implementation for " + AsciidoctorFactory.class);
                }
            }
            Instance.factory = iterator.next();
            return Instance.factory;
        }
        return Instance.factory;
    }


    Asciidoctor create();

    Asciidoctor create(String gemPath);

    Asciidoctor create(List<String> loadPaths);

    Asciidoctor create(ClassLoader classloader);

    Asciidoctor create(ClassLoader classloader, String gemPath);

    Asciidoctor create(List<String> loadPaths, String gemPath);

    ProcessorNodeFactory getProcessorNodeFactory();
}
