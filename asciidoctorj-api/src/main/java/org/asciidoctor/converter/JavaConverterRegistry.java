package org.asciidoctor.converter;

import org.asciidoctor.api.converter.Converter;
import org.asciidoctor.api.converter.OutputFormatWriter;

import java.util.Map;

public interface JavaConverterRegistry {

    <U, T  extends Converter<U> & OutputFormatWriter<U>> void register(Class<T> converterClass, String... backends);

    Class<?> resolve(String backend);

    void unregisterAll();

    Map<String, Class<?>> converters();
}
