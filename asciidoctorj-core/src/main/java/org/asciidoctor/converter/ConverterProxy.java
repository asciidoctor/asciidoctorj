package org.asciidoctor.converter;

import org.asciidoctor.ast.*;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConverterProxy extends RubyObject {

    private static final String BLOCK_CLASS = "Block";

    private static final String SECTION_CLASS = "Section";

    private static final String DOCUMENT_CLASS = "Document";

    private final Class<? extends Converter> converterClass;

    private Converter delegate;

    public ConverterProxy(Ruby runtime, RubyClass metaClass, Class<? extends Converter> converterClass) {
        super(runtime, metaClass);
        this.converterClass = converterClass;
    }

    @JRubyMethod(required = 2)
    public IRubyObject initialize(ThreadContext context, IRubyObject arg0, IRubyObject arg1) {
        try {
            Constructor<? extends Converter> constructor = converterClass.getConstructor(String.class, Map.class);
            this.delegate = constructor.newInstance(
                    RubyString.objAsString(context, arg0).asJavaString(),
                    JavaEmbedUtils.rubyToJava(getRuntime(), arg1, Map.class)
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
        Object ret = null;
        if (args.length == 1) {
            ret = delegate.convert(
                    overrideRubyObjectToJavaObject(args[0]),
                    null,
                    Collections.emptyMap());
        } else if (args.length == 2) {
            ret = delegate.convert(
                    overrideRubyObjectToJavaObject(args[0]),
                    (String) JavaEmbedUtils.rubyToJava(getRuntime(), args[1], String.class),
                    Collections.emptyMap());//RubyString.objAsString(context, args[1]).asJavaString());
        } else if (args.length == 3) {
            ret = delegate.convert(overrideRubyObjectToJavaObject(args[0]),
                    (String) JavaEmbedUtils.rubyToJava(getRuntime(), args[1], String.class),
                    (Map) JavaEmbedUtils.rubyToJava(getRuntime(), args[2], Map.class));
        }
        return JavaEmbedUtils.javaToRuby(getRuntime(), ret);
    }

    private AbstractBlock overrideRubyObjectToJavaObject(IRubyObject rubyObject) {
        // TODO: This is duplicated code. Move to a central location. It is the central logic of AsciidoctorJ!
        if (BLOCK_CLASS.equals(rubyObject.getMetaClass().getBaseName())) {
            Block blockRuby = RubyUtils.rubyToJava(getRuntime(), rubyObject, Block.class);
            return new BlockImpl(blockRuby, getRuntime());
        }
        else if (SECTION_CLASS.equals(rubyObject.getMetaClass().getBaseName())) {
            Section blockRuby = RubyUtils.rubyToJava(getRuntime(), rubyObject, Section.class);
            return new SectionImpl(blockRuby, getRuntime());
        }
        else if (DOCUMENT_CLASS.equals(rubyObject.getMetaClass().getBaseName())) {
            DocumentRuby blockRuby = RubyUtils.rubyToJava(getRuntime(), rubyObject, DocumentRuby.class);
            return new Document(blockRuby, getRuntime());
        }
        AbstractBlock blockRuby = RubyUtils.rubyToJava(getRuntime(), rubyObject, AbstractBlock.class);
        return new AbstractBlockImpl(blockRuby, getRuntime());
    }

}
