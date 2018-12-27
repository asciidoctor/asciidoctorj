package org.asciidoctor.internal;

import org.asciidoctor.api.converter.Converter;
import org.asciidoctor.api.converter.ConverterFor;
import org.asciidoctor.converter.ConverterProxy;
import org.asciidoctor.api.converter.OutputFormatWriter;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;

import java.util.HashMap;
import java.util.Map;

public class JavaConverterRegistryImpl implements JavaConverterRegistry {

    private Ruby rubyRuntime;

    public JavaConverterRegistryImpl(Ruby rubyRuntime) {
        this.rubyRuntime = rubyRuntime;
    }

    @Override
    public <U, T  extends Converter<U> & OutputFormatWriter<U>> void register(final Class<T> converterClass, String... backends) {

        RubyClass clazz = ConverterProxy.register(rubyRuntime, converterClass);

        ConverterFor converterForAnnotation = converterClass.getAnnotation(ConverterFor.class);
        if (converterForAnnotation != null) {
            // Backend annotation present => Register with name given in annotation
            String backend = !ConverterFor.UNDEFINED.equals(converterForAnnotation.format()) ? converterForAnnotation.format() : converterForAnnotation.value();
            getConverterFactory()
                .callMethod("register", clazz, rubyRuntime.newArray(rubyRuntime.newString(backend)));

        } else if (backends.length == 0) {
            // No backend annotation and no backend defined => register as default backend
            getConverterFactory()
                .callMethod("register", clazz);
        }
        if (backends.length > 0) {
            // Always additionally register with names passed to this method
            final RubyArray rubyBackendNames = new RubyArray(rubyRuntime, backends.length);
            for (String backend: backends) {
                rubyBackendNames.add(rubyRuntime.newString(backend));
            }
            getConverterFactory()
                .callMethod("register", clazz, rubyBackendNames);
        }
    }

    @Override
    public Class<?> resolve(String backend) {
        RubyClass rubyClass = (RubyClass) getConverterFactory()
            .callMethod("resolve", rubyRuntime.newString(backend));

        Class<?> clazz = rubyClass.getReifiedClass();
        if (clazz != null) {
            return clazz;
        } else if (rubyClass.getAllocator() instanceof ConverterProxy.Allocator) {
            ConverterProxy.Allocator allocator = (ConverterProxy.Allocator) rubyClass.getAllocator();
            return allocator.getConverterClass();
        }
        return null;
    }

    @Override
    public void unregisterAll() {
        getConverterFactory()
            .callMethod("unregister_all");
    }

    private RubyClass getConverterFactory() {
        return rubyRuntime.getModule("Asciidoctor")
            .defineOrGetModuleUnder("Converter")
            .getClass("Factory");
    }

    @Override
    public Map<String, Class<?>> converters() {
        final RubyArray rubyKeys = (RubyArray) getConverterFactory()
            .callMethod("converters")
            .callMethod(rubyRuntime.getCurrentContext(), "keys");

        Map<String, Class<?>> converters = new HashMap<String, Class<?>>();
        for (Object rubyBackend : rubyKeys) {
            String backend = rubyBackend.toString();
            converters.put(backend, resolve(backend));
        }
        return converters;
    }
}
