package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.extension.Processor;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyObject;

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
