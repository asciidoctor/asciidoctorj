package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.Postprocessor;
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

public class PostprocessorProxy extends RubyObject {

    private Class<? extends Postprocessor> postprocessorClass;

    private Postprocessor postprocessor;

    public PostprocessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends Postprocessor> postprocessorClass) {
        super(runtime, metaClass);
        this.postprocessorClass = postprocessorClass;
    }

    public PostprocessorProxy(Ruby runtime, RubyClass metaClass, Postprocessor postprocessor) {
        super(runtime, metaClass);
        this.postprocessor = postprocessor;
    }

    public static RubyClass register(final Ruby rubyRuntime, final String postprocessorClassName) {

        try {
            Class<? extends Postprocessor>  postprocessorClass = (Class<? extends Postprocessor>) Class.forName(postprocessorClassName);
            return register(rubyRuntime, postprocessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends Postprocessor> postprocessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "Postprocessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new PostprocessorProxy(runtime, klazz, postprocessor);
            }
        });
        rubyClass.defineAnnotatedMethods(PostprocessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final Postprocessor postprocessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "Postprocessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new PostprocessorProxy(runtime, klazz, postprocessor);
            }
        });
        rubyClass.defineAnnotatedMethods(PostprocessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject options) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (postprocessor != null) {
            // Instance was created in Java and has options set, so we pass these
            // instead of those passed by asciidoctor
            Helpers.invokeSuper(
                    context,
                    this,
                    getMetaClass(),
                    "initialize",
                    new IRubyObject[]{ JavaEmbedUtils.javaToRuby(getRuntime(), postprocessor.getConfig()) },
                    Block.NULL_BLOCK);
        } else {
            postprocessor = postprocessorClass.getConstructor(Map.class).newInstance(RubyUtils.rubyToJava(getRuntime(), options, Map.class));
            Helpers.invokeSuper(context, this, getMetaClass(), "initialize", new IRubyObject[]{options}, Block.NULL_BLOCK);
        }


        return null;
    }

    @JRubyMethod(name = "process", required = 2)
    public IRubyObject process(ThreadContext context, IRubyObject document, IRubyObject output) {
        return JavaEmbedUtils.javaToRuby(
                getRuntime(),
                postprocessor.process(
                        new Document(
                                RubyUtils.rubyToJava(getRuntime(), document, DocumentRuby.class),
                                getRuntime()),
                        RubyUtils.rubyToJava(getRuntime(), output, String.class)));
    }

}
