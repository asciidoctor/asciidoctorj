package org.asciidoctor.core.api.converter;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.converter.Converter;
import org.asciidoctor.converter.ConverterFor;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.converter.OutputFormatWriter;
import org.asciidoctor.core.api.CoreAsciidoctor;

/**
 * The {@link JavaConverterRegistry} of the AsciidoctorJ API: the converters are registered with the Asciidoctor
 * instance.
 */
public class JavaConverterRegistryImpl implements JavaConverterRegistry {
    /** The backend of a converter registered for all the backends that have no converter (catch all). */
    public static final String CATCH_ALL = "*";

    private final CoreAsciidoctor asciidoctor;

    public JavaConverterRegistryImpl(CoreAsciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
    }

    @Override
    public <U, T extends Converter<U> & OutputFormatWriter<U>> void register(Class<T> converterClass,
            String... backends) {
        Map<String, Class<?>> converters = asciidoctor.getConverters();
        ConverterFor converterFor = converterClass.getAnnotation(ConverterFor.class);
        if (converterFor != null) {
            // NOTE like AsciidoctorJ, the format takes precedence over the value of the annotation
            String backend = !ConverterFor.UNDEFINED.equals(converterFor.format()) ? converterFor.format()
                    : converterFor.value();
            converters.put(backend, converterClass);
        } else if (backends.length == 0) {
            converters.put(CATCH_ALL, converterClass);
        }
        for (String backend : backends) {
            converters.put(backend, converterClass);
        }
    }

    @Override
    public Class<?> resolve(String backend) {
        Map<String, Class<?>> converters = asciidoctor.getConverters();
        Class<?> converterClass = converters.get(backend);
        return converterClass != null ? converterClass : converters.get(CATCH_ALL);
    }

    @Override
    public void unregisterAll() {
        asciidoctor.getConverters().clear();
    }

    @Override
    public Map<String, Class<?>> converters() {
        // NOTE like AsciidoctorJ, a (mutable) copy of the registered converters
        return new HashMap<>(asciidoctor.getConverters());
    }
}
