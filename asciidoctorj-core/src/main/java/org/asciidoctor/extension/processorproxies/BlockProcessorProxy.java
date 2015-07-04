package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.AbstractNodeImpl;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Reader;
import org.asciidoctor.internal.RubyHashMapDecorator;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class BlockProcessorProxy extends AbstractProcessorProxy<BlockProcessor> {

    public BlockProcessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends BlockProcessor> blockProcessorClass) {
        super(runtime, metaClass, blockProcessorClass);
    }

    public BlockProcessorProxy(Ruby runtime, RubyClass metaClass, BlockProcessor blockProcessor) {
        super(runtime, metaClass, blockProcessor);
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends BlockProcessor> blockProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "BlockProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new BlockProcessorProxy(runtime, klazz, blockProcessor);
            }
        });

        applyAnnotations(blockProcessor, rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, BlockProcessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final BlockProcessor blockProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "BlockProcessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new BlockProcessorProxy(runtime, klazz, blockProcessor);
            }
        });

        applyAnnotations(blockProcessor.getClass(), rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, BlockProcessorProxy.class);
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
                    METHOD_NAME_INITIALIZE,
                    new IRubyObject[]{
                            JavaEmbedUtils.javaToRuby(getRuntime(), getProcessor().getName()),
                            RubyHashUtil.convertMapToRubyHashWithSymbols(getRuntime(), getProcessor().getConfig())},
                    Block.NULL_BLOCK);
            // The extension config in the Java extension is just a view on the @config member of the Ruby part
            getProcessor().setConfig(new RubyHashMapDecorator((RubyHash) getInstanceVariable(MEMBER_NAME_CONFIG)));
        } else {

            String macroName = RubyUtils.rubyToJava(getRuntime(), args[0], String.class);
            // First create only the instance passing in the block name
            setProcessor(instantiateProcessor(macroName, new HashMap<String, Object>()));

            if (getProcessor().getName() == null) {
                getProcessor().setName(macroName);
            }

            // Then create the config hash that may contain config options defined in the Java constructor
            RubyHash config = RubyHashUtil.convertMapToRubyHashWithSymbols(context.getRuntime(), getProcessor().getConfig());

            // Initialize the Ruby part and pass in the config options
            Helpers.invokeSuper(context, this, getMetaClass(), METHOD_NAME_INITIALIZE, new IRubyObject[] {args[0], config}, Block.NULL_BLOCK);

            // Reset the Java config options to the decorated Ruby hash, so that Java and Ruby work on the same config map
            getProcessor().setConfig(new RubyHashMapDecorator((RubyHash) getInstanceVariable(MEMBER_NAME_CONFIG)));
        }

        finalizeJavaConfig();

        return null;
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

    @JRubyMethod(name = "process", required = 3)
    public IRubyObject process(ThreadContext context, IRubyObject parent, IRubyObject reader, IRubyObject attributes) {
        Object o = getProcessor().process(
                (AbstractBlock) NodeConverter.createASTNode(parent),
                RubyUtils.rubyToJava(getRuntime(), reader, Reader.class),
                RubyUtils.rubyToJava(getRuntime(), attributes, Map.class));

        return convertProcessorResult(o);
    }

}
