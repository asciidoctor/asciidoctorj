package org.asciidoctor.jruby.extension.processorproxies;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyObject;

import java.util.concurrent.atomic.AtomicInteger;

public final class ProcessorProxyUtil {

    private ProcessorProxyUtil() {}

    private static AtomicInteger extensionCounter = new AtomicInteger();

    /**
     * For a simple Ruby class name like "Treeprocessor" it returns the associated RubyClass
     * from Asciidoctor::Extensions, e.g. Asciidoctor::Extensions::Treeprocessor
     * @param rubyRuntime
     * @param processorClassName
     * @return The Ruby class object for the given extension class name, e.g. Asciidoctor::Extensions::TreeProcessor
     */
    public static RubyClass getExtensionBaseClass(Ruby rubyRuntime, String processorClassName) {
        RubyModule extensionsModule = getExtensionsModule(rubyRuntime);
        return extensionsModule.getClass(processorClassName);
    }

    /**
     * Returns the Ruby module Asciidoc::Extensions.
     * @param rubyRuntime
     * @return The Ruby object for the module Asciidoc::Extensions.
     */
    private static RubyModule getExtensionsModule(Ruby rubyRuntime) {
        RubyModule asciidoctorModule = rubyRuntime.getModule("Asciidoctor");
        return asciidoctorModule.defineOrGetModuleUnder("Extensions");
    }

    public static RubyClass defineProcessorClass(Ruby rubyRuntime, String baseClassName, JRubyAsciidoctorObjectAllocator objectAllocator) {
        RubyClass baseClass = getExtensionBaseClass(rubyRuntime, baseClassName);
        final String rubyClassName = "JavaExtensionProxy_" + extensionCounter.getAndIncrement();
        return getExtensionsModule(baseClass.getRuntime()).defineClassUnder(rubyClassName, baseClass, objectAllocator);
    }

    /**
     * Defines the annotated methods of the given class and all super classes as
     * {@link org.jruby.RubyClass#defineAnnotatedMethods(Class)} does not handle inherited methods.
     * @param rubyClass
     * @param proxyClass
     */
    public static void defineAnnotatedMethods(RubyClass rubyClass, Class<?> proxyClass) {
        Class<?> currentClass = proxyClass;
        while (currentClass != RubyObject.class) {
            rubyClass.defineAnnotatedMethods(currentClass);
            currentClass = currentClass.getSuperclass();
        }
    }
}
