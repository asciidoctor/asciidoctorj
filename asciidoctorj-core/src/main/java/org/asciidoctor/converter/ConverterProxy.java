package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractNode;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.internal.RubyHashMapDecorator;
import org.asciidoctor.internal.RubyHashUtil;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

public class ConverterProxy extends RubyObject {

    protected static final String METHOD_NAME_INITIALIZE = "initialize";

    public static class Allocator implements ObjectAllocator {
        private final Class<? extends Converter> converterClass;

        public Allocator(Class<? extends Converter> converterClass) {
            this.converterClass = converterClass;
        }
        @Override
        public IRubyObject allocate(Ruby runtime, RubyClass rubyClass) {
            return new ConverterProxy(runtime, rubyClass, converterClass);
        }

        public Class<? extends Converter> getConverterClass() {
            return converterClass;
        }
    }

    private final Class<? extends Converter> converterClass;

    private Converter delegate;

    public ConverterProxy(Ruby runtime, RubyClass metaClass, Class<? extends Converter> converterClass) {
        super(runtime, metaClass);
        this.converterClass = converterClass;
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
        AbstractNode node = NodeConverter.createASTNode(args[0]);

        Object ret = null;
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
            ret = delegate.convert(
                    node,
                    (String) JavaEmbedUtils.rubyToJava(getRuntime(), args[1], String.class),
                    (Map) JavaEmbedUtils.rubyToJava(getRuntime(), args[2], Map.class));
        }
        return JavaEmbedUtils.javaToRuby(getRuntime(), ret);
    }


}
