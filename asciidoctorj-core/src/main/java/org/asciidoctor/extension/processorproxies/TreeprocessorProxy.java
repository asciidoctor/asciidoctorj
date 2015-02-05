package org.asciidoctor.extension.processorproxies;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.AbstractTreeProcessor;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
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

    private Class<? extends AbstractTreeProcessor> treeprocessorClass;

    private AbstractTreeProcessor treeProcessor;

    public TreeprocessorProxy(Ruby runtime, RubyClass metaClass, Class<? extends AbstractTreeProcessor> treeprocessorClass) {
        super(runtime, metaClass);
        this.treeprocessorClass = treeprocessorClass;
    }

    public static RubyClass register(final Ruby rubyRuntime, final Class<? extends AbstractTreeProcessor> treeProcessor) {
        // Get the base class
        System.out.println("REGISTER");
        RubyModule asciidoctorModule = rubyRuntime.getModule("Asciidoctor");
        RubyModule extensionsModule = asciidoctorModule.defineOrGetModuleUnder("Extensions");
        RubyClass baseClass = extensionsModule.getClass("Treeprocessor");

        RubyClass rubyClass = extensionsModule.defineClassUnder(treeProcessor.getSimpleName(), baseClass, new ObjectAllocator() {
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
        treeProcessor = treeprocessorClass.getConstructor(Map.class).newInstance(RubyUtils.rubyToJava(getRuntime(), options, Map.class));

        Helpers.invokeSuper(context, this, getMetaClass(), "initialize", new IRubyObject[]{options}, Block.NULL_BLOCK);

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
