package org.asciidoctor.converter;

import org.asciidoctor.internal.AsciidoctorModule;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyString;

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

    public void register(Class<? extends Converter> converterClass, String... backends) {
        // this may change in future to external class to deal with dynamic
        // imports
        String className = getImportLine(converterClass);
        this.rubyRuntime.evalScriptlet("java_import " + className);

        if (backends.length > 0) {
            this.asciidoctorModule.register_converter(RubyUtils.toRubyClass(rubyRuntime, converterClass), backends);
        } else {
            this.asciidoctorModule.register_converter(RubyUtils.toRubyClass(rubyRuntime, converterClass));
        }
    }

    public Class<?> resolve(String backend) {
        RubyClass rubyClass = this.asciidoctorModule.resolve_converter(backend);
        return rubyClass.getReifiedClass();
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

    private String getImportLine(Class<?> extensionClass) {

        int dollarPosition = -1;
        String className = extensionClass.getName();
        if ((dollarPosition = className.indexOf("$")) != -1) {
            className = className.substring(0, dollarPosition);
        }

        return className;
    }


}