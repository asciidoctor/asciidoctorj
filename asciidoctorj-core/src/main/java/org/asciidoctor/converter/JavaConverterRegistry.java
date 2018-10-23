package org.asciidoctor.converter;

import java.util.Map;

public interface JavaConverterRegistry {

    <U, T  extends Converter<U> & OutputFormatWriter<U>> void register(Class<T> converterClass, String... backends);

    Class<?> resolve(String backend);

    void unregisterAll();

    Map<String, Class<?>> converters();
}
