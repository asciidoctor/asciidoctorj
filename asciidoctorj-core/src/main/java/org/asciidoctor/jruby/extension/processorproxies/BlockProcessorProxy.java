package org.asciidoctor.jruby.extension.processorproxies;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.asciidoctor.jruby.extension.internal.ReaderImpl;
import org.asciidoctor.jruby.internal.*;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class BlockProcessorProxy extends AbstractProcessorProxy<BlockProcessor> {

    public BlockProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, Class<? extends BlockProcessor> blockProcessorClass) {
        super(asciidoctor, metaClass, blockProcessorClass);
    }

    public BlockProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, BlockProcessor blockProcessor) {
        super(asciidoctor, metaClass, blockProcessor);
    }

    public static RubyClass register(final JRubyAsciidoctor asciidoctor, final Class<? extends BlockProcessor> blockProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(asciidoctor.getRubyRuntime(), "BlockProcessor", new JRubyAsciidoctorObjectAllocator(asciidoctor) {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new BlockProcessorProxy(asciidoctor, klazz, blockProcessor);
            }
        });

        applyAnnotations(blockProcessor, rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, BlockProcessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final JRubyAsciidoctor asciidoctor, final BlockProcessor blockProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(asciidoctor.getRubyRuntime(), "BlockProcessor", new JRubyAsciidoctorObjectAllocator(asciidoctor) {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new BlockProcessorProxy(asciidoctor, klazz, blockProcessor);
            }
        });

        applyAnnotations(blockProcessor.getClass(), rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, BlockProcessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1, optional = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject[] args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        String explicitMacroName = RubyUtils.rubyToJava(getRuntime(), args[0], String.class);

        if (getProcessor() != null) {

            String macroName = explicitMacroName != null ? explicitMacroName : getProcessor().getName();

            // Instance was created in Java and has options set, so we pass these
            // instead of those passed by asciidoctor
            Helpers.invokeSuper(
                    context,
                    this,
                    getMetaClass(),
                    METHOD_NAME_INITIALIZE,
                    new IRubyObject[]{
                            JavaEmbedUtils.javaToRuby(getRuntime(), macroName),
                            RubyHashUtil.convertMapToRubyHashWithSymbols(getRuntime(), getProcessor().getConfig())},
                    Block.NULL_BLOCK);

            if (explicitMacroName != null) {
                getProcessor().setName(explicitMacroName);
            } else if (getProcessor().getName() == null) {
                RubyHash config = (RubyHash) this.callMethod(context, "config");
                Object rubyName = config.get(context.getRuntime().newSymbol("name"));
                if (rubyName != null) {
                    getProcessor().setName(rubyName.toString());
                }
            }

            // The extension config in the Java extension is just a view on the @config member of the Ruby part
            getProcessor().updateConfig(new RubyHashMapDecorator((RubyHash) getInstanceVariable(MEMBER_NAME_CONFIG)));
        } else {
            // First create only the instance passing in the block name
            setProcessor(instantiateProcessor(explicitMacroName, new HashMap<String, Object>()));

            getProcessor().setName(explicitMacroName);

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
                (StructuralNode) NodeConverter.createASTNode(parent),
                new ReaderImpl(reader),
                new RubyAttributesMapDecorator((RubyHash) attributes));

        return convertProcessorResult(o);
    }

}
