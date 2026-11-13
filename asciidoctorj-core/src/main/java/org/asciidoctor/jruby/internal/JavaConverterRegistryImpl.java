package org.asciidoctor.jruby.internal;

import org.asciidoctor.converter.Converter;
import org.asciidoctor.converter.ConverterFor;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.converter.OutputFormatWriter;
import org.asciidoctor.jruby.converter.internal.ConverterProxy;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.HashMap;
import java.util.Map;

public class JavaConverterRegistryImpl implements JavaConverterRegistry {

    private Ruby rubyRuntime;

    private JRubyAsciidoctor asciidoctor;

    public JavaConverterRegistryImpl(JRubyAsciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
        this.rubyRuntime = asciidoctor.getRubyRuntime();
    }


    @Override
    public <U, T  extends Converter<U> & OutputFormatWriter<U>> void register(final T converter, String... backends) {
        IRubyObject converterInstance = ConverterProxy.register(asciidoctor, converter);
        ConverterFor converterForAnnotation = converter.getClass().getAnnotation(ConverterFor.class);
        registerConverter(converterInstance, converterForAnnotation, backends);
    }

    @Override
    public <U, T  extends Converter<U> & OutputFormatWriter<U>> void register(final Class<T> converterClass, String... backends) {
        RubyClass clazz = ConverterProxy.register(asciidoctor, converterClass);
        ConverterFor converterForAnnotation = converterClass.getAnnotation(ConverterFor.class);
        registerConverter(clazz, converterForAnnotation, backends);
    }

    private void registerConverter(IRubyObject converter, ConverterFor converterForAnnotation, String[] backends) {
        if (converterForAnnotation != null) {
            // Backend annotation present => Register with name given in annotation
            String backend = !ConverterFor.UNDEFINED.equals(converterForAnnotation.format()) ? converterForAnnotation.format() : converterForAnnotation.value();
            getConverterFactory()
                .callMethod("register", converter, rubyRuntime.newString(backend));

        } else if (backends.length == 0) {
            // No backend annotation and no backend defined => register as default backend
            getConverterFactory()
                .callMethod("register", converter, rubyRuntime.newString("*"));
        }
        if (backends.length > 0) {
            // Always additionally register with names passed to this method
            for (String backend: backends) {
                getConverterFactory()
                        .callMethod("register", converter, rubyRuntime.newString(backend));
            }
        }
    }

    @Override
    public Class<?> resolve(String backend) {
        RubyClass rubyClass = (RubyClass) getConverterFactory()
            .callMethod("for", rubyRuntime.newString(backend));

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

    private RubyModule getConverterFactory() {
        return rubyRuntime.getModule("Asciidoctor")
            .getModule("Converter");
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