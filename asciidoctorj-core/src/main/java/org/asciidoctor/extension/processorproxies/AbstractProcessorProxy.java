package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.extension.Processor;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Map;

public class AbstractProcessorProxy<T extends Processor> extends RubyObject {

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

    @JRubyMethod(name = "config")
    public IRubyObject getConfig(ThreadContext context) {
        return RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(getRuntime(), getProcessor().getConfig());
    }

    @JRubyMethod(name = "config=", required = 1)
    public IRubyObject setConfig(ThreadContext context, IRubyObject newConfig) {
        processor.setConfig(RubyHashUtil.convertRubyHashMapToStringObjectMap((RubyHash) newConfig));
        return null;
    }

    @JRubyMethod(name = "update_config", required = 1)
    public IRubyObject updateConfig(ThreadContext context, IRubyObject additionalConfig) {
        Map additionalJavaConfig = RubyUtils.rubyToJava(getRuntime(), additionalConfig, Map.class);
        processor.update_config(additionalJavaConfig);
        return RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(getRuntime(), processor.getConfig());
    }

}
