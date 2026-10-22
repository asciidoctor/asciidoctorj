package org.asciidoctor.jruby.extension.processorproxies;

import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.DefaultAttribute;
import org.asciidoctor.extension.DefaultAttributes;
import org.asciidoctor.extension.Format;
import org.asciidoctor.extension.Location;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.PositionalAttributes;
import org.asciidoctor.extension.Processor;
import org.asciidoctor.jruby.ast.impl.ContentNodeImpl;
import org.asciidoctor.jruby.extension.internal.JRubyProcessor;
import org.asciidoctor.jruby.internal.Extensions;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.RubyRegexp;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.RegexpOptions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class AbstractProcessorProxy<T extends Processor> extends RubyObject {

    protected static final String MEMBER_NAME_CONFIG = "@config";
    protected static final String METHOD_NAME_INITIALIZE = "initialize";

    private JRubyAsciidoctor asciidoctor;

    protected T processor;

    protected JRubyProcessor processorDelegate;

    private Class<? extends T> processorClass;

    public AbstractProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, Class<? extends T> processorClass) {
        super(asciidoctor.getRubyRuntime(), metaClass);
        this.asciidoctor = asciidoctor;
        this.processorClass = processorClass;
    }

    public AbstractProcessorProxy(JRubyAsciidoctor asciidoctor, RubyClass metaClass, T processor) {
        super(asciidoctor.getRubyRuntime(), metaClass);
        this.asciidoctor = asciidoctor;
        processor.unwrap(JRubyProcessor.class).setAsciidoctor(asciidoctor);
        this.processor = processor;
        this.processorDelegate = processor.unwrap(JRubyProcessor.class);
    }

    protected T getProcessor() {
        return processor;
    }

    protected JRubyProcessor getProcessorDelegate() {
        return processorDelegate;
    }

    protected JRubyAsciidoctor getAsciidoctor() {
        return asciidoctor;
    }

    protected void setProcessor(T processor) {
        this.processor = processor;
        this.processorDelegate = processor.unwrap(JRubyProcessor.class);
    }

    public Class<? extends T> getProcessorClass() {
        return processorClass;
    }

    public void setProcessorClass(Class<? extends T> processorClass) {
        this.processorClass = processorClass;
    }

    T instantiateProcessor(Object... args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends T> constructor = Extensions.findConstructorWithMostMatchingArguments(getProcessorClass(), args);
        T processor = constructor.newInstance(Arrays.copyOf(args, constructor.getParameterTypes().length));
        processor.unwrap(JRubyProcessor.class).setAsciidoctor(this.asciidoctor);
        return processor;
    }


    public void finalizeJavaConfig() {
        getProcessorDelegate().setConfigFinalized();
    }

    protected IRubyObject convertProcessorResult(Object o) {
        if (o instanceof ContentNodeImpl) {
            return ((ContentNodeImpl) o).getRubyObject();
        } else {
            return JavaEmbedUtils.javaToRuby(getRuntime(), o);
        }
    }

    protected static void applyAnnotations(Class<? extends Processor> processor, RubyClass rubyClass) {
        handleLocationAnnotation(processor, rubyClass);

        handleNameAnnotation(processor, rubyClass);

        handleContentModelAnnotation(processor, rubyClass);

        handleDefaultAttributeAnnotation(processor, rubyClass);

        handleDefaultAttributesAnnotation(processor, rubyClass);

        handlePositionalAttributesAnnotation(processor, rubyClass);

        handleContextsAnnotation(processor, rubyClass);

        handleFormatAnnotation(processor, rubyClass);
    }

    private static void handleFormatAnnotation(Class<? extends Processor> processor, RubyClass rubyClass) {
        Ruby rubyRuntime = rubyClass.getRuntime();
        if (processor.isAnnotationPresent(Format.class)) {
            Format format = processor.getAnnotation(Format.class);
            switch (format.value()) {
                case CUSTOM:
                    rubyClass.callMethod(rubyRuntime.getCurrentContext(), "option", new IRubyObject[]{
                            rubyRuntime.newSymbol("regexp"),
                            convertRegexp(rubyRuntime, format.regexp())
                    });
                default:
                    rubyClass.callMethod(rubyRuntime.getCurrentContext(), "option", new IRubyObject[]{
                            rubyRuntime.newSymbol("format"),
                            rubyRuntime.newSymbol(format.value().optionValue().substring(1))
                    });
            }
        }
    }

    private static void handleContextsAnnotation(Class<? extends Processor> processor, RubyClass rubyClass) {
        Ruby rubyRuntime = rubyClass.getRuntime();
        if (processor.isAnnotationPresent(Contexts.class)) {
            Contexts contexts = processor.getAnnotation(Contexts.class);
            RubyArray contextList = rubyRuntime.newArray();
            for (String value : contexts.value()) {
                contextList.add(rubyRuntime.newSymbol(value.substring(1)));
            }
            rubyClass.callMethod(rubyRuntime.getCurrentContext(), "option", new IRubyObject[]{
                    rubyRuntime.newSymbol("contexts"),
                    contextList
            });

        }
    }

    private static void handleDefaultAttributesAnnotation(Class<? extends Processor> processor, RubyClass rubyClass) {
        Ruby rubyRuntime = rubyClass.getRuntime();
        if (processor.isAnnotationPresent(DefaultAttributes.class)) {
            DefaultAttributes defaultAttributes = processor.getAnnotation(DefaultAttributes.class);
            RubyHash defaultAttrs = RubyHash.newHash(rubyRuntime);
            for (DefaultAttribute defaultAttribute : defaultAttributes.value()) {
                defaultAttrs.put(defaultAttribute.key(), defaultAttribute.value());
            }
            rubyClass.callMethod(rubyRuntime.getCurrentContext(), "option", new IRubyObject[]{
                    rubyRuntime.newSymbol("default_attrs"),
                    defaultAttrs
            });
        }
    }

    private static void handleDefaultAttributeAnnotation(Class<? extends Processor> processor, RubyClass rubyClass) {
        Ruby rubyRuntime = rubyClass.getRuntime();
        if (processor.isAnnotationPresent(DefaultAttribute.class)) {
            DefaultAttribute defaultAttribute = processor.getAnnotation(DefaultAttribute.class);
            RubyHash defaultAttrs = RubyHash.newHash(rubyRuntime);
            defaultAttrs.put(defaultAttribute.key(), defaultAttribute.value());
            rubyClass.callMethod(rubyRuntime.getCurrentContext(), "option", new IRubyObject[]{
                    rubyRuntime.newSymbol("default_attrs"),
                    defaultAttrs
            });
        }
    }

    private static void handlePositionalAttributesAnnotation(Class<? extends Processor> processor, RubyClass rubyClass) {
        Ruby rubyRuntime = rubyClass.getRuntime();
        if (processor.isAnnotationPresent(PositionalAttributes.class)) {
            PositionalAttributes positionalAttributes = processor.getAnnotation(PositionalAttributes.class);
            RubyArray positionalAttrs = RubyArray.newArray(rubyRuntime);
            for (String positionalAttribute : positionalAttributes.value()) {
                positionalAttrs.add(positionalAttribute);
            }
            rubyClass.callMethod(rubyRuntime.getCurrentContext(), "option", new IRubyObject[]{
                    rubyRuntime.newSymbol("positional_attrs"),
                    positionalAttrs
            });
        }
    }

    private static void handleContentModelAnnotation(Class<? extends Processor> processor, RubyClass rubyClass) {
        Ruby rubyRuntime = rubyClass.getRuntime();
        if (processor.isAnnotationPresent(ContentModel.class)) {
            ContentModel contentModel = processor.getAnnotation(ContentModel.class);
            rubyClass.callMethod(rubyRuntime.getCurrentContext(), "option", new IRubyObject[]{
                    rubyRuntime.newSymbol("content_model"),
                    rubyRuntime.newSymbol(contentModel.value().substring(1))
            });
        }
    }

    private static void handleNameAnnotation(Class<? extends Processor> processor, RubyClass rubyClass) {
        Ruby rubyRuntime = rubyClass.getRuntime();
        if (processor.isAnnotationPresent(Name.class)) {
            Name name = processor.getAnnotation(Name.class);
            rubyClass.callMethod(rubyRuntime.getCurrentContext(), "option", new IRubyObject[]{
                    rubyRuntime.newSymbol("name"),
                    rubyRuntime.newString(name.value())
            });
        }
    }

    private static void handleLocationAnnotation(Class<? extends Processor> processor, RubyClass rubyClass) {
        Ruby rubyRuntime = rubyClass.getRuntime();
        if (processor.isAnnotationPresent(Location.class)) {
            Location location = processor.getAnnotation(Location.class);
            rubyClass.callMethod(rubyRuntime.getCurrentContext(), "option", new IRubyObject[]{
                    rubyRuntime.newSymbol("location"),
                    rubyRuntime.newSymbol(location.value().optionValue().substring(1))
            });
        }
    }

    protected static RubyRegexp convertRegexp(Ruby runtime, CharSequence regexp) {
        return RubyRegexp.newRegexp(runtime, regexp.toString(), RegexpOptions.NULL_OPTIONS);
    }

    public static String getName(Class<? extends Processor> processor) {
        if (processor.isAnnotationPresent(Name.class)) {
            Name name = processor.getAnnotation(Name.class);
            return name.value();
        }
        throw new IllegalArgumentException("Processor " + processor + " has no @Name annotation!");
    }
}
