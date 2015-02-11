package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class InlineMacroProcessorProxy extends AbstractMacroProcessorProxy<InlineMacroProcessor> {

    private static String blockName;

    public InlineMacroProcessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends InlineMacroProcessor> inlineMacroProcessorClass, String blockName) {
        super(runtime, metaClass, inlineMacroProcessorClass);
        this.blockName = blockName;
    }

    public InlineMacroProcessorProxy(Ruby runtime, RubyClass metaClass, InlineMacroProcessor inlineMacroProcessor) {
        super(runtime, metaClass, inlineMacroProcessor);
        this.blockName = inlineMacroProcessor.getName();
    }

    public static RubyClass register(final Ruby rubyRuntime, final String inlineMacroProcessorClassName, final String blockName) {

        try {
            Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessorClassName);
            return register(rubyRuntime, inlineMacroProcessorClass, blockName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends InlineMacroProcessor> inlineMacroProcessor, final String blockName) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "InlineMacroProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new InlineMacroProcessorProxy(runtime, klazz, inlineMacroProcessor, blockName);
            }
        });
        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, InlineMacroProcessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final InlineMacroProcessor inlineMacroProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "InlineMacroProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new InlineMacroProcessorProxy(runtime, klazz, inlineMacroProcessor);
            }
        });
        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, InlineMacroProcessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1, optional = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (getProcessor() != null) {
            // Instance was created in Java and has options set, so we pass these
            // instead of those passed by asciidoctor
            Helpers.invokeSuper(
                    context,
                    this,
                    getMetaClass(),
                    "initialize",
                    new IRubyObject[]{
                            JavaEmbedUtils.javaToRuby(getRuntime(), getProcessor().getName()),
                            JavaEmbedUtils.javaToRuby(getRuntime(), getProcessor().getConfig())},
                    Block.NULL_BLOCK);
        } else {
            if (args.length == 1) {
                setProcessor(
                        getProcessorClass()
                                .getConstructor(String.class)
                                .newInstance(RubyUtils.rubyToJava(getRuntime(), args[0], String.class)));
            } else {
                setProcessor(
                        getProcessorClass()
                                .getConstructor(String.class, Map.class)
                                .newInstance(
                                        RubyUtils.rubyToJava(getRuntime(), args[0], String.class),
                                        RubyUtils.rubyToJava(getRuntime(), args[1], Map.class)));
            }
            Helpers.invokeSuper(context, this, getMetaClass(), "initialize", args, Block.NULL_BLOCK);
        }
        return null;
    }

    @JRubyMethod(name = "process", required = 3)
    public IRubyObject process(ThreadContext context, IRubyObject parent, IRubyObject target, IRubyObject attributes) {
        return JavaEmbedUtils.javaToRuby(
                getRuntime(),
                getProcessor().process(
                        (AbstractBlock) NodeConverter.createASTNode(parent),
                        RubyUtils.rubyToJava(getRuntime(), target, String.class),
                        RubyUtils.rubyToJava(getRuntime(), attributes, Map.class)));
    }

}
