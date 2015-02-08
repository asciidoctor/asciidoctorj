package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.extension.Processor;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.runtime.ObjectAllocator;

import java.util.concurrent.atomic.AtomicInteger;

public final class ProcessorProxyUtil {

    private ProcessorProxyUtil() {}

    private static AtomicInteger extensionCounter = new AtomicInteger();

    /**
     * For a simple Ruby class name like "Treeprocessor" it returns the associated RubyClass
     * from Asciidoctor::Extensions, e.g. Asciidoctor::Extensions::Treeprocessor
     * @param rubyRuntime
     * @param processorClassName
     * @return
     */
    public static RubyClass getExtensionBaseClass(Ruby rubyRuntime, String processorClassName) {
        RubyModule extensionsModule = getExtensionsModule(rubyRuntime);
        return extensionsModule.getClass(processorClassName);
    }

    /**
     * Returns the Ruby module Asciidoc::Extensions.
     * @param rubyRuntime
     * @return
     */
    private static RubyModule getExtensionsModule(Ruby rubyRuntime) {
        RubyModule asciidoctorModule = rubyRuntime.getModule("Asciidoctor");
        return asciidoctorModule.defineOrGetModuleUnder("Extensions");
    }

    public static RubyClass defineProcessorClass(Ruby rubyRuntime, String baseClassName, ObjectAllocator objectAllocator) {
        RubyClass baseClass = getExtensionBaseClass(rubyRuntime, baseClassName);
        final String rubyClassName = "JavaExtensionProxy_" + extensionCounter.getAndIncrement();
        return getExtensionsModule(baseClass.getRuntime()).defineClassUnder(rubyClassName, baseClass, objectAllocator);
    }

}
