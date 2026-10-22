package org.asciidoctor.core.api.syntaxhighlighter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.asciidoctor.core.AbstractBlock;
import org.asciidoctor.core.Document;
import org.asciidoctor.core.api.ast.NodeConverter;
import org.asciidoctor.core.syntaxhighlighter.AbstractSyntaxHighlighter;
import org.asciidoctor.extension.LocationType;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.Logging;
import org.asciidoctor.syntaxhighlighter.Formatter;
import org.asciidoctor.syntaxhighlighter.Highlighter;
import org.asciidoctor.syntaxhighlighter.StylesheetWriter;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterAdapter;

/**
 * The syntax highlighter of the implementation that proxies a syntax highlighter of the AsciidoctorJ API (like the
 * SyntaxHighlighterProxy of AsciidoctorJ). The optional interfaces ({@link Highlighter}, {@link Formatter} and
 * {@link StylesheetWriter}) are honored if the syntax highlighter implements them.
 */
public class SyntaxHighlighterProxy extends AbstractSyntaxHighlighter {
    private final SyntaxHighlighterAdapter delegate;

    public SyntaxHighlighterProxy(Class<? extends SyntaxHighlighterAdapter> highlighterClass, String name,
            String backend, Map<String, Object> opts, LogHandler logHandler) {
        super(name, backend, opts);
        this.delegate = instantiate(highlighterClass, name, backend, apiOptions(opts));
        if (delegate instanceof Logging logging) {
            logging.setLogHandler(logHandler);
        }
    }

    private static SyntaxHighlighterAdapter instantiate(Class<? extends SyntaxHighlighterAdapter> type, String name,
            String backend, Map<String, Object> opts) {
        Object[] args = { name, backend, opts };
        // NOTE Constructor (inherited from SyntaxHighlighter) is the constructor function of the implementation
        java.lang.reflect.Constructor<?> best = null;
        for (java.lang.reflect.Constructor<?> constructor : type.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            boolean matches = parameterTypes.length <= args.length;
            for (int i = 0; matches && i < parameterTypes.length; i++) {
                matches = parameterTypes[i].isInstance(args[i]);
            }
            if (matches && (best == null || parameterTypes.length > best.getParameterCount())) {
                best = constructor;
            }
        }
        if (best == null) {
            throw new IllegalArgumentException("No suitable constructor found for " + type.getName());
        }
        try {
            Object[] actualArgs = new Object[best.getParameterCount()];
            System.arraycopy(args, 0, actualArgs, 0, actualArgs.length);
            return (SyntaxHighlighterAdapter) best.newInstance(actualArgs);
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof RuntimeException runtimeException ? runtimeException
                    : new IllegalStateException(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not instantiate syntax highlighter: " + type.getName(), e);
        }
    }

    /**
     * @return the options of the API that correspond to the options of the implementation (e.g., the document)
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> apiOptions(Map<String, Object> opts) {
        return opts == null ? new java.util.LinkedHashMap<>() : (Map<String, Object>) NodeConverter.toApi(opts);
    }

    private static LocationType locationType(String location) {
        return "footer".equals(location) ? LocationType.FOOTER : LocationType.HEADER;
    }

    @Override
    public boolean hasDocinfo(String location) {
        return delegate.hasDocInfo(locationType(location));
    }

    @Override
    public String getDocinfo(String location, Document doc, Map<String, Object> opts) {
        return delegate.getDocinfo(locationType(location), NodeConverter.wrap(doc), apiOptions(opts));
    }

    @Override
    public boolean isHighlight() {
        return delegate instanceof Highlighter;
    }

    @Override
    public HighlightResult highlight(AbstractBlock node, String source, String lang, Map<String, Object> opts) {
        // NOTE HighlightResult (inherited from SyntaxHighlighter) is the result of the implementation
        org.asciidoctor.syntaxhighlighter.HighlightResult result = ((Highlighter) delegate).highlight(
                NodeConverter.wrap(node), source, lang, apiOptions(opts));
        return new HighlightResult(result.getHighlightedSource(), result.getLineOffset());
    }

    @Override
    public String format(AbstractBlock node, String lang, Map<String, Object> opts) {
        if (delegate instanceof Formatter formatter) {
            return formatter.format(NodeConverter.wrap(node), lang, apiOptions(opts));
        }
        return super.format(node, lang, opts);
    }

    @Override
    public boolean isWriteStylesheet(Document doc) {
        return delegate instanceof StylesheetWriter writer && writer.isWriteStylesheet(NodeConverter.wrap(doc));
    }

    @Override
    public void writeStylesheet(Document doc, String toDir) {
        ((StylesheetWriter) delegate).writeStylesheet(NodeConverter.wrap(doc), new File(toDir));
    }
}
