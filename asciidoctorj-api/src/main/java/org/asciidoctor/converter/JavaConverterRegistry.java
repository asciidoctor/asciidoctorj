package org.asciidoctor.converter;

import java.util.Map;

public interface JavaConverterRegistry {

    public <U, T  extends Converter<U> & OutputFormatWriter<U>> void register(final Class<T> converterClass, String... backends);

    public Class<?> resolve(String backend);

    public void unregisterAll();

    public Map<String, Class<?>> converters();

}