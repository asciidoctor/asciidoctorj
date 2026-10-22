package org.asciidoctor.jruby.converter.internal;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.converter.Converter;
import org.asciidoctor.converter.ConverterFor;
import org.asciidoctor.converter.OutputFormatWriter;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.jruby.internal.RubyHashMapDecorator;
import org.asciidoctor.jruby.internal.RubyHashUtil;
import org.asciidoctor.jruby.internal.RubyOutputStreamWrapper;
import org.asciidoctor.log.Logging;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.anno.JRubyMethod;
import org.jruby.java.proxies.JavaProxy;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

public class ConverterProxy<T> extends RubyObject {

    protected static final String METHOD_NAME_INITIALIZE = "initialize";
    private final JRubyAsciidoctor asciidoctor;

    private T output;

    public static <U, T  extends Converter<U> & OutputFormatWriter<U>> RubyClass register(JRubyAsciidoctor asciidoctor, final Class<T> converterClass) {
        Ruby rubyRuntime = asciidoctor.getRubyRuntime();
        RubyModule module = rubyRuntime.defineModule(getModuleName(converterClass));
        RubyClass clazz = module.defineClassUnder(
                converterClass.getSimpleName(),
                rubyRuntime.getObject(),
                new ConverterProxy.Allocator(converterClass, asciidoctor));
        includeModule(clazz, "Asciidoctor", "Converter");
        includeModule(clazz, "Asciidoctor", "Converter", "BackendTraits");

        clazz.defineAnnotatedMethod(ConverterProxy.class, "initialize");
        clazz.defineAnnotatedMethod(ConverterProxy.class, "convert");

        includeModule(clazz, "Asciidoctor", "Writer");
        clazz.defineAnnotatedMethod(ConverterProxy.class, "write");

        //clazz.defineAnnotatedMethods(ConverterProxy.class);
        return clazz;
    }

    public static class Allocator implements ObjectAllocator {
        private final Class<? extends Converter> converterClass;
        private final JRubyAsciidoctor asciidoctor;

        public Allocator(Class<? extends Converter> converterClass, JRubyAsciidoctor asciidoctor) {
            this.converterClass = converterClass;
            this.asciidoctor = asciidoctor;
        }
        @Override
        public IRubyObject allocate(Ruby runtime, RubyClass rubyClass) {
            return new ConverterProxy(runtime, rubyClass, converterClass, asciidoctor);
        }

        public Class<? extends Converter> getConverterClass() {
            return converterClass;
        }
    }

    private final Class<? extends Converter> converterClass;

    private Converter<T> delegate;

    public ConverterProxy(Ruby runtime, RubyClass metaClass, Class<? extends Converter> converterClass, JRubyAsciidoctor asciidoctor) {
        super(runtime, metaClass);
        this.converterClass = converterClass;
        this.asciidoctor = asciidoctor;
    }

    @JRubyMethod(required = 1, optional = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject[] args) {
        try {
            String backend = (String) JavaEmbedUtils.rubyToJava(getRuntime(), args[0], String.class);

            Constructor<? extends Converter> constructor = converterClass.getConstructor(String.class, Map.class);
            this.delegate = constructor.newInstance(
                    backend,
                    args.length < 2  ? Collections.emptyMap() : new RubyHashMapDecorator((RubyHash) args[1])
            );
            if (this.delegate instanceof Logging) {
                ((Logging) this.delegate).setLogHandler(asciidoctor);
            }
            // Initialize the Ruby part and pass in the config options
            RubyHash options = RubyHashUtil.convertMapToRubyHashWithSymbols(context.getRuntime(), this.delegate.getOptions());
            Helpers.invokeSuper(context, this, getMetaClass(), METHOD_NAME_INITIALIZE, new IRubyObject[]{args[0], options}, Block.NULL_BLOCK);

            String outfileSuffix = getOutfileSuffix();
            if (outfileSuffix != null) {
                this.callMethod(context, "outfilesuffix", getRuntime().newString(outfileSuffix));
            }

            return null;
        } catch (InstantiationException e) {
            // TODO: Do some proper logging in the catch clauses?
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String getOutfileSuffix() {
        String outfileSuffix = this.delegate.getOutfileSuffix();
        if (outfileSuffix == null && delegate.getClass().getAnnotation(ConverterFor.class) != null) {
            ConverterFor converterAnnotation = delegate.getClass().getAnnotation(ConverterFor.class);
            if (converterAnnotation.suffix() != ConverterFor.UNDEFINED) {
                outfileSuffix = converterAnnotation.suffix();
                this.delegate.setOutfileSuffix(outfileSuffix);
            }
        }
        return outfileSuffix;
    }

    @JRubyMethod(required = 1, optional = 2)
    public IRubyObject convert(ThreadContext context, IRubyObject[] args) {
        ContentNode node = NodeConverter.createASTNode(args[0]);

        T ret = null;
        if (args.length == 1) {
            ret = delegate.convert(
                    node,
                    null,
                    Collections.emptyMap());
        } else if (args.length == 2) {
            ret = delegate.convert(
                    node,
                    (String) JavaEmbedUtils.rubyToJava(getRuntime(), args[1], String.class),
                    Collections.emptyMap());//RubyString.objAsString(context, args[1]).asJavaString());
        } else if (args.length == 3) {
            ret = (T) delegate.convert(
                    node,
                    (String) JavaEmbedUtils.rubyToJava(getRuntime(), args[1], String.class),
                    (Map) JavaEmbedUtils.rubyToJava(getRuntime(), args[2], Map.class));
        }
        this.output = ret;
        return JavaEmbedUtils.javaToRuby(getRuntime(), ret);
    }

    @JRubyMethod
    public IRubyObject write(ThreadContext context, IRubyObject output, IRubyObject target) throws IOException {
        OutputStream out = null;
        if (target instanceof JavaProxy) {
            JavaProxy javaProxy = (JavaProxy) target;
            if (javaProxy.getObject() instanceof OutputStream) {
                out = (OutputStream) ((JavaProxy) target).getObject();
            } else {
                throw new IllegalArgumentException("Can't write to a " + javaProxy.getObject());
            }
        } else if (target instanceof RubyString) {
            File f = new File(((RubyString) target).asJavaString());
            out = new FileOutputStream(f);
        } else if (target instanceof RubyOutputStreamWrapper) {
            out = ((RubyOutputStreamWrapper)target).getOut();
        } else {
            throw new IllegalArgumentException("Can't write to a " + target);
        }
        if (out != null) {
            try {
                ((OutputFormatWriter<T>) delegate).write(this.output, out);
            } finally {
                out.close();
            }
        }
        return null;
    }


    private static void includeModule(RubyClass clazz, String moduleName, String... moduleNames) {
        RubyModule module = clazz.getRuntime().getModule(moduleName);
        if (moduleNames != null && moduleNames.length > 0) {
            for (String submoduleName: moduleNames) {
                module = module.getModule(submoduleName);
            }
        }
        clazz.includeModule(module);
    }

    private static String getModuleName(Class<?> converterClass) {
        StringBuilder sb = new StringBuilder();
        for (String s: converterClass.getPackage().getName().split("\\.")) {
            sb
                    .append(s.substring(0, 1).toUpperCase())
                    .append(s.substring(1).toLowerCase());
        }
        return sb.toString();
    }


}
