package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class IncludeProcessorProxy extends RubyObject {

    private Class<? extends IncludeProcessor> includeProcessorClass;

    private IncludeProcessor includeProcessor;

    public IncludeProcessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends IncludeProcessor> includeProcessorClass) {
        super(runtime, metaClass);
        this.includeProcessorClass = includeProcessorClass;
    }

    public IncludeProcessorProxy(Ruby runtime, RubyClass metaClass, IncludeProcessor includeProcessor) {
        super(runtime, metaClass);
        this.includeProcessor = includeProcessor;
    }

    public static RubyClass register(final Ruby rubyRuntime, final String includeProcessorClassName) {

        try {
            Class<? extends IncludeProcessor>  includeProcessorClass = (Class<? extends IncludeProcessor>) Class.forName(includeProcessorClassName);
            return register(rubyRuntime, includeProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends IncludeProcessor> includeProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "IncludeProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new IncludeProcessorProxy(runtime, klazz, includeProcessor);
            }
        });
        rubyClass.defineAnnotatedMethods(IncludeProcessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final IncludeProcessor includeProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "IncludeProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new IncludeProcessorProxy(runtime, klazz, includeProcessor);
            }
        });
        rubyClass.defineAnnotatedMethods(IncludeProcessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject options) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (includeProcessor != null) {
            // Instance was created in Java and has options set, so we pass these
            // instead of those passed by asciidoctor
            Helpers.invokeSuper(
                    context,
                    this,
                    getMetaClass(),
                    "initialize",
                    new IRubyObject[]{ JavaEmbedUtils.javaToRuby(getRuntime(), includeProcessor.getConfig()) },
                    Block.NULL_BLOCK);
        } else {
            includeProcessor = includeProcessorClass.getConstructor(Map.class).newInstance(RubyUtils.rubyToJava(getRuntime(), options, Map.class));
            Helpers.invokeSuper(context, this, getMetaClass(), "initialize", new IRubyObject[]{options}, Block.NULL_BLOCK);
        }


        return null;
    }

    @JRubyMethod(name = "handles", required = 1)
    public IRubyObject handles(ThreadContext context, IRubyObject target) {
        boolean b = includeProcessor.handles(RubyUtils.rubyToJava(getRuntime(), target, String.class));
        return JavaEmbedUtils.javaToRuby(getRuntime(), b);
    }


    @JRubyMethod(name = "process", required = 4)
    public IRubyObject process(ThreadContext context, IRubyObject[] args) {
        DocumentRuby document =
                new Document(
                        RubyUtils.rubyToJava(getRuntime(), args[0], DocumentRuby.class),
                        getRuntime());
        PreprocessorReader reader = RubyUtils.rubyToJava(getRuntime(), args[1], PreprocessorReader.class);
        String target = RubyUtils.rubyToJava(getRuntime(), args[2], String.class);
        Map<String, Object> attributes = RubyUtils.rubyToJava(getRuntime(), args[3], Map.class);
        includeProcessor.process(document, reader, target, attributes);
        return null;
    }

}
