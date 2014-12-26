package org.asciidoctor.converter;

import org.asciidoctor.internal.AsciidoctorModule;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyString;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.HashMap;
import java.util.Map;

public class ConverterRegistry {

    private AsciidoctorModule asciidoctorModule;
    private Ruby rubyRuntime;

    public ConverterRegistry(AsciidoctorModule asciidoctorModule, Ruby rubyRuntime) {
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
        if (backends.length > 0) {
            this.asciidoctorModule.register_converter(clazz, backends);
        } else {
            this.asciidoctorModule.register_converter(clazz);
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