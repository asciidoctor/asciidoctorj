package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.api.extension.MacroProcessor;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public abstract class AbstractMacroProcessorProxy<T extends MacroProcessor> extends AbstractProcessorProxy<T> {

    public AbstractMacroProcessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends T> processorClass) {
        super(runtime, metaClass, processorClass);
    }

    public AbstractMacroProcessorProxy(Ruby runtime, RubyClass metaClass, T processor) {
        super(runtime, metaClass, processor);
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
