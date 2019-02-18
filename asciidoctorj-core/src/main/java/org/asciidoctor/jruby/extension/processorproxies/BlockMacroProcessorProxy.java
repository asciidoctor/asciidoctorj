package org.asciidoctor.jruby.extension.processorproxies;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
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

public class BlockMacroProcessorProxy extends AbstractMacroProcessorProxy<BlockMacroProcessor> {

    public BlockMacroProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, Class<? extends BlockMacroProcessor> blockMacroProcessorClass) {
        super(asciidoctor, metaClass, blockMacroProcessorClass);
    }

    public BlockMacroProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, BlockMacroProcessor blockMacroProcessor) {
        super(asciidoctor, metaClass, blockMacroProcessor);
    }

    public static RubyClass register(final JRubyAsciidoctor asciidoctor, final Class<? extends BlockMacroProcessor> blockMacroProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(asciidoctor.getRubyRuntime(), "BlockMacroProcessor", new JRubyAsciidoctorObjectAllocator(asciidoctor) {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new BlockMacroProcessorProxy(asciidoctor, klazz, blockMacroProcessor);
            }
        });

        applyAnnotations(blockMacroProcessor, rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, BlockMacroProcessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final JRubyAsciidoctor asciidoctor, final BlockMacroProcessor blockMacroProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(asciidoctor.getRubyRuntime(), "BlockMacroProcessor", new JRubyAsciidoctorObjectAllocator(asciidoctor) {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new BlockMacroProcessorProxy(asciidoctor, klazz, blockMacroProcessor);
            }
        });

        applyAnnotations(blockMacroProcessor.getClass(), rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, BlockMacroProcessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1, optional = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
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
            getProcessor().updateConfig(new RubyHashMapDecorator((RubyHash)getInstanceVariable(MEMBER_NAME_CONFIG)));
        } else {
            // First create only the instance passing in the name
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

    @JRubyMethod(name = "process", required = 3)
    public IRubyObject process(ThreadContext context, IRubyObject parent, IRubyObject target, IRubyObject attributes) {
        Object o = getProcessor().process(
                (StructuralNode) NodeConverter.createASTNode(parent),
                RubyUtils.rubyToJava(getRuntime(), target, String.class),
                new RubyAttributesMapDecorator((RubyHash) attributes));

        return convertProcessorResult(o);

    }

}
