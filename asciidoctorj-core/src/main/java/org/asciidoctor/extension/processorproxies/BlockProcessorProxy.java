package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Reader;
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

public class BlockProcessorProxy extends RubyObject {

    private Class<? extends BlockProcessor> blockProcessorClass;

    private BlockProcessor blockProcessor;

    public BlockProcessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends BlockProcessor> blockProcessorClass) {
        super(runtime, metaClass);
        this.blockProcessorClass = blockProcessorClass;
    }

    public BlockProcessorProxy(Ruby runtime, RubyClass metaClass, BlockProcessor blockProcessor) {
        super(runtime, metaClass);
        this.blockProcessor = blockProcessor;
    }

    public static RubyClass register(final Ruby rubyRuntime, final String blockProcessorClassName) {

        try {
            Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessorClassName);
            return register(rubyRuntime, blockProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends BlockProcessor> blockProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "BlockProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new BlockProcessorProxy(runtime, klazz, blockProcessor);
            }
        });
        rubyClass.defineAnnotatedMethods(BlockProcessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final BlockProcessor blockProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "BlockProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new BlockProcessorProxy(runtime, klazz, blockProcessor);
            }
        });
        rubyClass.defineAnnotatedMethods(BlockProcessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1, optional = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (blockProcessor != null) {
            // Instance was created in Java and has options set, so we pass these
            // instead of those passed by asciidoctor
            Helpers.invokeSuper(
                    context,
                    this,
                    getMetaClass(),
                    "initialize",
                    new IRubyObject[]{
                            JavaEmbedUtils.javaToRuby(getRuntime(), blockProcessor.getName()),
                            JavaEmbedUtils.javaToRuby(getRuntime(), blockProcessor.getConfig()) },
                    Block.NULL_BLOCK);
        } else {
            if (args.length == 1) {
                blockProcessor = blockProcessorClass.getConstructor(String.class).newInstance(RubyUtils.rubyToJava(getRuntime(), args[0], String.class));
            } else {
                blockProcessor =
                        blockProcessorClass
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
        return JavaEmbedUtils.javaToRuby(getRuntime(), blockProcessor.getName());
    }

    @JRubyMethod(name = "name=", required = 1)
    public IRubyObject setName(ThreadContext context, IRubyObject name) {
        blockProcessor.setName(RubyUtils.rubyToJava(getRuntime(), name, String.class));
        return null;
    }

    //    public abstract Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes);
    @JRubyMethod(name = "process", required = 3)
    public IRubyObject process(ThreadContext context, IRubyObject parent, IRubyObject reader, IRubyObject attributes) {
        return JavaEmbedUtils.javaToRuby(
                getRuntime(),
                blockProcessor.process(
                        (AbstractBlock) NodeConverter.createASTNode(parent),
                        RubyUtils.rubyToJava(getRuntime(), reader, Reader.class),
                        RubyUtils.rubyToJava(getRuntime(), attributes, Map.class)));
    }

}
