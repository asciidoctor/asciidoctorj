package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.extension.BlockMacroProcessor;
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

public class BlockMacroProcessorProxy extends AbstractMacroProcessorProxy<BlockMacroProcessor> {

    public BlockMacroProcessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends BlockMacroProcessor> blockMacroProcessorClass) {
        super(runtime, metaClass, blockMacroProcessorClass);
    }

    public BlockMacroProcessorProxy(Ruby runtime, RubyClass metaClass, BlockMacroProcessor blockMacroProcessor) {
        super(runtime, metaClass, blockMacroProcessor);
    }

    public static RubyClass register(final Ruby rubyRuntime, final String blockMacroProcessorClassName) {

        try {
            Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessorClassName);
            return register(rubyRuntime, blockMacroProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "BlockMacroProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new BlockMacroProcessorProxy(runtime, klazz, blockMacroProcessor);
            }
        });
        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, BlockMacroProcessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final BlockMacroProcessor blockMacroProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "BlockMacroProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new BlockMacroProcessorProxy(runtime, klazz, blockMacroProcessor);
            }
        });
        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, BlockMacroProcessorProxy.class);
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
                            JavaEmbedUtils.javaToRuby(getRuntime(), getProcessor().getConfig()) },
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
