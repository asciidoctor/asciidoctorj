package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.AbstractNodeImpl;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.extension.Postprocessor;
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

public class PostprocessorProxy extends AbstractProcessorProxy<Postprocessor> {

    public PostprocessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends Postprocessor> postprocessorClass) {
        super(runtime, metaClass, postprocessorClass);
    }

    public PostprocessorProxy(Ruby runtime, RubyClass metaClass, Postprocessor postprocessor) {
        super(runtime, metaClass, postprocessor);
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends Postprocessor> postprocessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "Postprocessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new PostprocessorProxy(runtime, klazz, postprocessor);
            }
        });

        applyAnnotations(postprocessor, rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, PostprocessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final Postprocessor postprocessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "Postprocessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new PostprocessorProxy(runtime, klazz, postprocessor);
            }
        });

        applyAnnotations(postprocessor.getClass(), rubyClass);

        ProcessorProxyUtil.defineAnnotatedMethods(rubyClass, PostprocessorProxy.class);
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
            getProcessor().setConfig(new RubyHashMapDecorator((RubyHash) getInstanceVariable(MEMBER_NAME_CONFIG)));
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

    @JRubyMethod(name = "process", required = 2)
    public IRubyObject process(ThreadContext context, IRubyObject document, IRubyObject output) {
        Object o = getProcessor().process(
                (DocumentRuby) NodeConverter.createASTNode(document),
                RubyUtils.rubyToJava(getRuntime(), output, String.class));

        return convertProcessorResult(o);
    }

}
