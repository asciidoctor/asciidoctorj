package org.asciidoctor.converter;

import org.asciidoctor.internal.AsciidoctorModule;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyString;

import java.util.HashMap;
import java.util.Map;

public class JavaConverterRegistry {

    private AsciidoctorModule asciidoctorModule;
    private Ruby rubyRuntime;

    public JavaConverterRegistry(AsciidoctorModule asciidoctorModule, Ruby rubyRuntime) {
        super();
        this.asciidoctorModule = asciidoctorModule;
        this.rubyRuntime = rubyRuntime;
    }

    public void register(final Class<? extends Converter> converterClass, String... backends) {
        RubyModule module = rubyRuntime.defineModule(getModuleName(converterClass));
        RubyClass clazz = module.defineClassUnder(
                converterClass.getSimpleName(),
                rubyRuntime.getObject(),
                new ConverterProxy.Allocator(converterClass));
        clazz.defineAnnotatedMethods(ConverterProxy.class);

        ConverterFor converterForAnnotation = converterClass.getAnnotation(ConverterFor.class);
        if (converterForAnnotation != null) {
            // Backend annotation present => Register with name given in annotation
            this.asciidoctorModule.register_converter(clazz, new String[] { converterForAnnotation.value() });
        } else if (backends.length == 0) {
            // No backend annotation and no backend defined => register as default backend
            this.asciidoctorModule.register_converter(clazz);
        }
        if (backends.length > 0) {
            // Always additionally register with names passed to this method
            this.asciidoctorModule.register_converter(clazz, backends);
        }
    }

    private String getModuleName(Class<?> converterClass) {
        StringBuilder sb = new StringBuilder();
        for (String s: converterClass.getPackage().getName().split("\\.")) {
            sb
                    .append(s.substring(0, 1).toUpperCase())
                    .append(s.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    public Class<?> resolve(String backend) {
        RubyClass rubyClass = this.asciidoctorModule.resolve_converter(backend);
        Class<?> clazz = rubyClass.getReifiedClass();
        if (clazz != null) {
            return clazz;
        } else if (rubyClass.getAllocator() instanceof ConverterProxy.Allocator) {
            ConverterProxy.Allocator allocator = (ConverterProxy.Allocator) rubyClass.getAllocator();
            return allocator.getConverterClass();
        }
        return null;
    }

    public void unregisterAll() {
        this.asciidoctorModule.unregister_all_converters();
    }

    public Map<String, Class<?>> converters() {
        RubyArray rubyKeys = this.asciidoctorModule.converters();

        Map<String, Class<?>> converters = new HashMap<String, Class<?>>();
        for (Object rubyBackend : rubyKeys.getList()) {
            String backend = ((RubyString) rubyBackend).asJavaString();
            converters.put(backend, resolve(backend));
        }
        return converters;
    }
}