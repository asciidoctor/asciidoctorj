package org.asciidoctor.jruby.extension.processorproxies;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.jruby.internal.RubyHashMapDecorator;
import org.asciidoctor.jruby.internal.RubyHashUtil;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class TreeprocessorProxy extends AbstractProcessorProxy<Treeprocessor> {

    public TreeprocessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, Class<? extends Treeprocessor> treeprocessorClass) {
        super(asciidoctor, metaClass, treeprocessorClass);
    }

    public TreeprocessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, Treeprocessor treeProcessor) {
        super(asciidoctor, metaClass, treeProcessor);
    }

    public static RubyClass register(final JRubyAsciidoctor asciidoctor, final Class<? extends Treeprocessor> treeProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(asciidoctor.getRubyRuntime(), "Treeprocessor", new JRubyAsciidoctorObjectAllocator(asciidoctor) {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new TreeprocessorProxy(asciidoctor, klazz, treeProcessor);
            }
        });

        applyAnnotations(treeProcessor, rubyClass);

        rubyClass.defineAnnotatedMethods(TreeprocessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final JRubyAsciidoctor asciidoctor, final Treeprocessor treeProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(asciidoctor.getRubyRuntime(), "Treeprocessor", new JRubyAsciidoctorObjectAllocator(asciidoctor) {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new TreeprocessorProxy(this.asciidoctor, klazz, treeProcessor);
            }
        });

        applyAnnotations(treeProcessor.getClass(), rubyClass);

        rubyClass.defineAnnotatedMethods(TreeprocessorProxy.class);
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
            Helpers.invokeSuper(context, this, getMetaClass(), METHOD_NAME_INITIALIZE, new IRubyObject[]{config}, Block.NULL_BLOCK);

            // Reset the Java config options to the decorated Ruby hash, so that Java and Ruby work on the same config map
            getProcessor().setConfig(new RubyHashMapDecorator((RubyHash) getInstanceVariable(MEMBER_NAME_CONFIG)));
        }

        finalizeJavaConfig();

        return null;
    }

    @JRubyMethod(name = "process", required = 1)
    public IRubyObject process(ThreadContext context, IRubyObject document) {
        Object o = getProcessor().process(
                (Document) NodeConverter.createASTNode(document));
        return convertProcessorResult(o);
    }
}
