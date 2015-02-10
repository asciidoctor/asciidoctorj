package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
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

public class InlineMacroProcessorProxy extends RubyObject {

    private Class<? extends InlineMacroProcessor> inlineMacroProcessorClass;

    private InlineMacroProcessor inlineMacroProcessor;

    public InlineMacroProcessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends InlineMacroProcessor> inlineMacroProcessorClass) {
        super(runtime, metaClass);
        this.inlineMacroProcessorClass = inlineMacroProcessorClass;
    }

    public InlineMacroProcessorProxy(Ruby runtime, RubyClass metaClass, InlineMacroProcessor inlineMacroProcessor) {
        super(runtime, metaClass);
        this.inlineMacroProcessor = inlineMacroProcessor;
    }

    public static RubyClass register(final Ruby rubyRuntime, final String inlineMacroProcessorClassName) {

        try {
            Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessorClassName);
            return register(rubyRuntime, inlineMacroProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "InlineMacroProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new InlineMacroProcessorProxy(runtime, klazz, inlineMacroProcessor);
            }
        });
        rubyClass.defineAnnotatedMethods(InlineMacroProcessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final InlineMacroProcessor inlineMacroProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "InlineMacroProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new InlineMacroProcessorProxy(runtime, klazz, inlineMacroProcessor);
            }
        });
        rubyClass.defineAnnotatedMethods(InlineMacroProcessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1, optional = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (inlineMacroProcessor != null) {
            // Instance was created in Java and has options set, so we pass these
            // instead of those passed by asciidoctor
            Helpers.invokeSuper(
                    context,
                    this,
                    getMetaClass(),
                    "initialize",
                    new IRubyObject[]{
                            JavaEmbedUtils.javaToRuby(getRuntime(), inlineMacroProcessor.getName()),
                            JavaEmbedUtils.javaToRuby(getRuntime(), inlineMacroProcessor.getConfig()) },
                    Block.NULL_BLOCK);
        } else {
            if (args.length == 1) {
                inlineMacroProcessor = inlineMacroProcessorClass.getConstructor(String.class).newInstance(RubyUtils.rubyToJava(getRuntime(), args[0], String.class));
            } else {
                inlineMacroProcessor =
                        inlineMacroProcessorClass
                                .getConstructor(String.class, Map.class)
                                .newInstance(
                                        RubyUtils.rubyToJava(getRuntime(), args[0], String.class),
                                        RubyUtils.rubyToJava(getRuntime(), args[1], Map.class));
            }
            Helpers.invokeSuper(context, this, getMetaClass(), "initialize", args, Block.NULL_BLOCK);
        }


        return null;
    }

    @JRubyMethod(name = "name", required = 0)
    public IRubyObject getName(ThreadContext context) {
        return JavaEmbedUtils.javaToRuby(getRuntime(), inlineMacroProcessor.getName());
    }

    @JRubyMethod(name = "name=", required = 1)
    public IRubyObject setName(ThreadContext context, IRubyObject name) {
        inlineMacroProcessor.setName(RubyUtils.rubyToJava(getRuntime(), name, String.class));
        return null;
    }

    @JRubyMethod(name = "process", required = 3)
    public IRubyObject process(ThreadContext context, IRubyObject parent, IRubyObject target, IRubyObject attributes) {
        return JavaEmbedUtils.javaToRuby(
                getRuntime(),
                inlineMacroProcessor.process(
                        (AbstractBlock) NodeConverter.createASTNode(parent),
                        RubyUtils.rubyToJava(getRuntime(), target, String.class),
                        RubyUtils.rubyToJava(getRuntime(), attributes, Map.class)));
    }

}
