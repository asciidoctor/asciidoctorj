package org.asciidoctor.jruby.extension.processorproxies;

import org.asciidoctor.extension.MacroProcessor;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.jruby.internal.RubyUtils;
import org.jruby.RubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public abstract class AbstractMacroProcessorProxy<T extends MacroProcessor> extends AbstractProcessorProxy<T> {

    public AbstractMacroProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, Class<? extends T> processorClass) {
        super(asciidoctor, metaClass, processorClass);
    }

    public AbstractMacroProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, T processor) {
        super(asciidoctor, metaClass, processor);
    }

    @JRubyMethod(name = "name", required = 0)
    public IRubyObject getName(ThreadContext context) {
        return JavaEmbedUtils.javaToRuby(getRuntime(), getProcessor().getName());
    }

    @JRubyMethod(name = "name=", required = 1)
    public IRubyObject setName(ThreadContext context, IRubyObject name) {
        getProcessor().setName(RubyUtils.rubyToJava(getRuntime(), name, String.class));
        return null;
    }
}
