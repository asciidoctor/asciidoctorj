package org.asciidoctor.jruby.extension.processorproxies;

import org.asciidoctor.ast.Document;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.jruby.extension.internal.PreprocessorReaderImpl;
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
import java.util.Map;

public class IncludeProcessorProxy extends AbstractProcessorProxy<IncludeProcessor> {

    public IncludeProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, Class<? extends IncludeProcessor> includeProcessorClass) {
        super(asciidoctor, metaClass, includeProcessorClass);
    }

    public IncludeProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, IncludeProcessor includeProcessor) {
        super(asciidoctor, metaClass, includeProcessor);
    }

    public static RubyClass register(final JRubyAsciidoctor asciidoctor, final Class<? extends IncludeProcessor> includeProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(asciidoctor.getRubyRuntime(), "IncludeProcessor", new JRubyAsciidoctorObjectAllocator(asciidoctor) {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new IncludeProcessorProxy(asciidoctor, klazz, includeProcessor);
            }
        });

        applyAnnotations(includeProcessor, rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, IncludeProcessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final JRubyAsciidoctor asciidoctor, final IncludeProcessor includeProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(asciidoctor.getRubyRuntime(), "IncludeProcessor", new JRubyAsciidoctorObjectAllocator(asciidoctor) {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new IncludeProcessorProxy(asciidoctor, klazz, includeProcessor);
            }
        });

        applyAnnotations(includeProcessor.getClass(), rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, IncludeProcessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject options) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        if (getProcessor() != null) {
            // Instance was created in Java and has options set, so we pass these
            // instead of those passed by asciidoctor
            Helpers.invokeSuper(
                    context,
                    this,
                    getMetaClass(),
                    METHOD_NAME_INITIALIZE,
                    new IRubyObject[]{
                            RubyHashUtil.convertMapToRubyHashWithSymbols(getRuntime(), getProcessor().getConfig())},
                    Block.NULL_BLOCK);
            // The extension config in the Java extension is just a view on the @config member of the Ruby part
            getProcessor().updateConfig(new RubyHashMapDecorator((RubyHash) getInstanceVariable(MEMBER_NAME_CONFIG)));
        } else {
            // First create only the instance passing in the block name
            setProcessor(instantiateProcessor(new HashMap<String, Object>()));

            // Then create the config hash that may contain config options defined in the Java constructor
            RubyHash config = RubyHashUtil.convertMapToRubyHashWithSymbols(context.getRuntime(), getProcessor().getConfig());

            // Initialize the Ruby part and pass in the config options
            Helpers.invokeSuper(context, this, getMetaClass(), METHOD_NAME_INITIALIZE, new IRubyObject[] {config}, Block.NULL_BLOCK);

            // Reset the Java config options to the decorated Ruby hash, so that Java and Ruby work on the same config map
            getProcessor().setConfig(new RubyHashMapDecorator((RubyHash) getInstanceVariable(MEMBER_NAME_CONFIG)));
        }

        finalizeJavaConfig();

        return null;
    }

    @JRubyMethod(name = "handles?", required = 1)
    public IRubyObject handles(ThreadContext context, IRubyObject target) {
        boolean b = getProcessor().handles(RubyUtils.rubyToJava(getRuntime(), target, String.class));
        return JavaEmbedUtils.javaToRuby(getRuntime(), b);
    }


    @JRubyMethod(name = "process", required = 4)
    public IRubyObject process(ThreadContext context, IRubyObject[] args) {
        Document document = (Document) NodeConverter.createASTNode(args[0]);
        PreprocessorReader reader = new PreprocessorReaderImpl(args[1]);
        String target = RubyUtils.rubyToJava(getRuntime(), args[2], String.class);
        Map<String, Object> attributes = new RubyAttributesMapDecorator((RubyHash) args[3]);
        getProcessor().process(document, reader, target, attributes);
        return null;
    }

}
