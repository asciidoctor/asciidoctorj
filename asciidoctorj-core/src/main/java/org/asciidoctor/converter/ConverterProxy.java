package org.asciidoctor.converter;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.AbstractNode;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.BlockImpl;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.ast.Inline;
import org.asciidoctor.ast.InlineImpl;
import org.asciidoctor.ast.ListImpl;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.ListItemImpl;
import org.asciidoctor.ast.ListNode;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.SectionImpl;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConverterProxy extends RubyObject {

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
            Constructor<? extends Converter> constructor = converterClass.getConstructor(String.class, Map.class);
            this.delegate = constructor.newInstance(
                    JavaEmbedUtils.rubyToJava(getRuntime(), args[0], String.class),
                    args.length < 2  ? Collections.emptyMap() : JavaEmbedUtils.rubyToJava(getRuntime(), args[1], Map.class)
            );
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
