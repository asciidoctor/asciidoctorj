package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.Treeprocessor;
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

public class TreeprocessorProxy extends RubyObject {

    private Class<? extends Treeprocessor> treeprocessorClass;

    private Treeprocessor treeProcessor;

    public TreeprocessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends Treeprocessor> treeprocessorClass) {
        super(runtime, metaClass);
        this.treeprocessorClass = treeprocessorClass;
    }

    public TreeprocessorProxy(Ruby runtime, RubyClass metaClass, Treeprocessor treeProcessor) {
        super(runtime, metaClass);
        this.treeProcessor = treeProcessor;
    }

    public static RubyClass register(final Ruby rubyRuntime, final String treeProcessorClassName) {

        try {
            Class<? extends Treeprocessor>  treeProcessorClass = (Class<? extends Treeprocessor>) Class.forName(treeProcessorClassName);
            return register(rubyRuntime, treeProcessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends Treeprocessor> treeProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "Treeprocessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new TreeprocessorProxy(runtime, klazz, treeProcessor);
            }
        });
        rubyClass.defineAnnotatedMethods(TreeprocessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final Treeprocessor treeProcessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "Treeprocessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new TreeprocessorProxy(runtime, klazz, treeProcessor);
            }
        });
        rubyClass.defineAnnotatedMethods(TreeprocessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject options) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (treeProcessor != null) {
            // Instance was created in Java and has options set, so we pass these
            // instead of those passed by asciidoctor
            Helpers.invokeSuper(
                    context,
                    this,
                    getMetaClass(),
                    "initialize",
                    new IRubyObject[]{JavaEmbedUtils.javaToRuby(getRuntime(), treeProcessor.getConfig())},
                    Block.NULL_BLOCK);
        } else {
            treeProcessor = treeprocessorClass.getConstructor(Map.class).newInstance(RubyUtils.rubyToJava(getRuntime(), options, Map.class));
            Helpers.invokeSuper(context, this, getMetaClass(), "initialize", new IRubyObject[]{options}, Block.NULL_BLOCK);
        }


        return null;
    }

    @JRubyMethod(name = "process", required = 1)
    public IRubyObject process(ThreadContext context, IRubyObject document) {
        return JavaEmbedUtils.javaToRuby(
                getRuntime(),
                treeProcessor.process(
                    new Document(
                            RubyUtils.rubyToJava(getRuntime(), document, DocumentRuby.class),
                            getRuntime())));
    }

}
