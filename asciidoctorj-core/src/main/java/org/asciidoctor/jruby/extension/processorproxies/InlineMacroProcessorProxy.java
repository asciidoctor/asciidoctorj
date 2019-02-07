package org.asciidoctor.jruby.extension.processorproxies;

import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.asciidoctor.jruby.internal.*;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubySymbol;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.InvocationTargetException;

public class InlineMacroProcessorProxy extends AbstractMacroProcessorProxy<InlineMacroProcessor> {

    private static final String SUPER_CLASS_NAME = "InlineMacroProcessor";

    public InlineMacroProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, Class<? extends InlineMacroProcessor> inlineMacroProcessorClass) {
        super(asciidoctor, metaClass, inlineMacroProcessorClass);
    }

    public InlineMacroProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, InlineMacroProcessor inlineMacroProcessor) {
        super(asciidoctor, metaClass, inlineMacroProcessor);
    }

    public static RubyClass register(final JRubyAsciidoctor asciidoctor, final Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(asciidoctor.getRubyRuntime(), SUPER_CLASS_NAME, new JRubyAsciidoctorObjectAllocator(asciidoctor) {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new InlineMacroProcessorProxy(asciidoctor, klazz, inlineMacroProcessor);
            }
        });

        applyAnnotations(inlineMacroProcessor, rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, InlineMacroProcessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final JRubyAsciidoctor asciidoctor, final InlineMacroProcessor inlineMacroProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(asciidoctor.getRubyRuntime(), SUPER_CLASS_NAME, new JRubyAsciidoctorObjectAllocator(asciidoctor) {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new InlineMacroProcessorProxy(asciidoctor, klazz, inlineMacroProcessor);
            }
        });

        applyAnnotations(inlineMacroProcessor.getClass(), rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, InlineMacroProcessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1, optional = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String explicitMacroName = RubyUtils.rubyToJava(getRuntime(), args[0], String.class);

        if (getProcessor() != null) {
            // Instance was created in Java and has options set, so we pass these
            // instead of those passed by asciidoctor

            // If options contains a String with a Regexp create the RubyRegexp from it
            RubyHash rubyConfig = RubyHashUtil.convertMapToRubyHashWithSymbols(getRuntime(), getProcessor().getConfig());
            Object regexp = getProcessor().getConfig().get("regexp");
            if (regexp != null && regexp instanceof CharSequence) {
                RubySymbol regexpSymbol = RubySymbol.newSymbol(getRuntime(), "regexp");
                rubyConfig.put(regexpSymbol, convertRegexp(getRuntime(), (CharSequence) regexp));
            }

            String macroName = explicitMacroName != null ? explicitMacroName : getProcessor().getName();

            Helpers.invokeSuper(
                    context,
                    this,
                    getMetaClass(),
                    METHOD_NAME_INITIALIZE,
                    new IRubyObject[]{
                            JavaEmbedUtils.javaToRuby(getRuntime(), macroName),
                            rubyConfig},
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

            // The Ruby initialize method may have changed the config, therefore copy it back
            // because the accessor is routed to the Java Processor.config
            getProcessor().updateConfig(new RubyHashMapDecorator((RubyHash) getInstanceVariable(MEMBER_NAME_CONFIG)));
        } else {
            // First create only the instance passing in the block name
            setProcessor(instantiateProcessor(explicitMacroName));

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
                NodeConverter.createASTNode(parent),
                RubyUtils.rubyToJava(getRuntime(), target, String.class),
                new RubyAttributesMapDecorator((RubyHash) attributes));
        return convertProcessorResult(o);
    }

}
