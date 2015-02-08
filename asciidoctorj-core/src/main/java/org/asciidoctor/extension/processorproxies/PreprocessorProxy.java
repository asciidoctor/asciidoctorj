package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
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

public class PreprocessorProxy extends RubyObject {

    private Class<? extends Preprocessor> preprocessorClass;

    private Preprocessor preprocessor;

    public PreprocessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends Preprocessor> preprocessorClass) {
        super(runtime, metaClass);
        this.preprocessorClass = preprocessorClass;
    }

    public PreprocessorProxy(Ruby runtime, RubyClass metaClass, Preprocessor preprocessor) {
        super(runtime, metaClass);
        this.preprocessor = preprocessor;
    }

    public static RubyClass register(final Ruby rubyRuntime, final String preprocessorClassName) {

        try {
            Class<? extends Preprocessor>  preprocessorClass = (Class<? extends Preprocessor>) Class.forName(preprocessorClassName);
            return register(rubyRuntime, preprocessorClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends Preprocessor> preprocessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "Preprocessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new PreprocessorProxy(runtime, klazz, preprocessor);
            }
        });
        rubyClass.defineAnnotatedMethods(PreprocessorProxy.class);
        return rubyClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final Preprocessor preprocessor) {
        RubyClass rubyClass = ProcessorProxyUtil.defineProcessorClass(rubyRuntime, "Preprocessor", new ObjectAllocator() {
            @Override
            public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
                return new PreprocessorProxy(runtime, klazz, preprocessor);
            }
        });
        rubyClass.defineAnnotatedMethods(PreprocessorProxy.class);
        return rubyClass;
    }

    @JRubyMethod(name = "initialize", required = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject options) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (preprocessor != null) {
            // Instance was created in Java and has options set, so we pass these
            // instead of those passed by asciidoctor
            Helpers.invokeSuper(
                    context,
                    this,
                    getMetaClass(),
                    "initialize",
                    new IRubyObject[]{ JavaEmbedUtils.javaToRuby(getRuntime(), preprocessor.getConfig()) },
                    Block.NULL_BLOCK);
        } else {
            preprocessor = preprocessorClass.getConstructor(Map.class).newInstance(RubyUtils.rubyToJava(getRuntime(), options, Map.class));
            Helpers.invokeSuper(context, this, getMetaClass(), "initialize", new IRubyObject[]{options}, Block.NULL_BLOCK);
        }


        return null;
    }

    @JRubyMethod(name = "process", required = 2)
    public IRubyObject process(ThreadContext context, IRubyObject document, IRubyObject preprocessorReader) {
        return JavaEmbedUtils.javaToRuby(
                getRuntime(),
                preprocessor.process(
                        new Document(
                                RubyUtils.rubyToJava(getRuntime(), document, DocumentRuby.class),
                                getRuntime()),
                        RubyUtils.rubyToJava(getRuntime(), preprocessorReader, PreprocessorReader.class)));
    }

}
