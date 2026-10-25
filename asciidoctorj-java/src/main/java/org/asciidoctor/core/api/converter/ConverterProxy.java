package org.asciidoctor.core.api.converter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.asciidoctor.converter.Converter;
import org.asciidoctor.converter.ConverterFor;
import org.asciidoctor.converter.OutputFormatWriter;
import org.asciidoctor.core.AbstractNode;
import org.asciidoctor.core.Writer;
import org.asciidoctor.core.api.ast.NodeConverter;
import org.asciidoctor.core.converter.AbstractConverter;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.Logging;

/**
 * The converter of the implementation that proxies a converter of the AsciidoctorJ API (like the ConverterProxy of
 * AsciidoctorJ).
 */
public class ConverterProxy extends AbstractConverter<Object> implements Writer<Object> {
    private final Converter<Object> delegate;

    @SuppressWarnings("unchecked")
    public ConverterProxy(Class<?> converterClass, String backend, Map<String, Object> opts, LogHandler logHandler) {
        super(backend, opts);
        try {
            this.delegate = (Converter<Object>) converterClass.getConstructor(String.class, Map.class)
                    .newInstance(backend, apiOptions(opts));
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof RuntimeException runtimeException ? runtimeException
                    : new IllegalStateException(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not instantiate converter: " + converterClass.getName(), e);
        }
        if (delegate instanceof Logging logging) {
            logging.setLogHandler(logHandler);
        }
        String outfilesuffix = delegate.getOutfileSuffix();
        ConverterFor converterFor = converterClass.getAnnotation(ConverterFor.class);
        if (outfilesuffix == null && converterFor != null && !ConverterFor.UNDEFINED.equals(converterFor.suffix())) {
            outfilesuffix = converterFor.suffix();
            delegate.setOutfileSuffix(outfilesuffix);
        }
        if (outfilesuffix != null) {
            setOutfilesuffix(outfilesuffix);
        }
    }

    /**
     * @return the converter of the API
     */
    public Converter<Object> getDelegate() {
        return delegate;
    }

    private static Map<String, Object> apiOptions(Map<String, Object> opts) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (opts != null) {
            opts.forEach((key, value) -> result.put(key, NodeConverter.toApi(value)));
        }
        return result;
    }

    @Override
    public Object convert(AbstractNode node, String transform, Map<String, Object> opts) {
        Map<Object, Object> apiOpts = new LinkedHashMap<>();
        if (opts != null) {
            opts.forEach((key, value) -> apiOpts.put(key, NodeConverter.toApi(value)));
        }
        return delegate.convert(NodeConverter.wrap(node), transform, apiOpts);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(Object output, Object target) {
        if (!(delegate instanceof OutputFormatWriter<?>)) {
            Writer.super.write(output, target);
            return;
        }
        OutputFormatWriter<Object> writer = (OutputFormatWriter<Object>) delegate;
        try {
            if (target instanceof OutputStream out) {
                writer.write(output, out);
                out.flush();
            } else if (target instanceof Appendable) {
                Writer.super.write(output, target);
            } else {
                try (OutputStream out = new FileOutputStream(String.valueOf(target))) {
                    writer.write(output, out);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
