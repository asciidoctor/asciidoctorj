package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.extension.Processor;
import org.asciidoctor.internal.RubyHashMapDecorator;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Map;

public class AbstractProcessorProxy<T extends Processor> extends RubyObject {

    protected static final String MEMBER_NAME_CONFIG = "@config";
    protected static final String METHOD_NAME_INITIALIZE = "initialize";

    protected T processor;

    private Class<? extends T> processorClass;

    public AbstractProcessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends T> processorClass) {
        super(runtime, metaClass);
        this.processorClass = processorClass;
    }

    public AbstractProcessorProxy(Ruby runtime, RubyClass metaClass, T processor) {
        super(runtime, metaClass);
        this.processor = processor;
    }

    protected T getProcessor() {
        return processor;
    }

    protected void setProcessor(T processor) {
        this.processor = processor;
    }

    public Class<? extends T> getProcessorClass() {
        return processorClass;
    }

    public void setProcessorClass(Class<? extends T> processorClass) {
        this.processorClass = processorClass;
    }

}
