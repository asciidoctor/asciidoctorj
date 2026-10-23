package org.asciidoctor.core.extension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.asciidoctor.core.AbstractNode;
import org.asciidoctor.core.Asciidoctor;
import org.asciidoctor.core.Rx;

/**
 * InlineMacroProcessors are used to handle inline macros that have a custom name.
 *
 * <p>InlineMacroProcessor implementations must extend InlineMacroProcessor.
 */
// TODO break this out into different pattern types
// for example, FullInlineMacro, ShortInlineMacro (no target) and other patterns
// FIXME for inline macro, we need to have some way to specify the text as a passthrough
public class InlineMacroProcessor extends MacroProcessor {
    private static final Map<String, Pattern> RX_CACHE = new ConcurrentHashMap<>();

    /** The function that implements the process method when using the DSL. */
    @FunctionalInterface
    public interface ProcessFunction {
        /**
         * @return an Inline node or a String replacement
         */
        Object process(AbstractNode parent, String target, Map<String, Object> attributes);
    }

    private ProcessFunction processFunction;

    public InlineMacroProcessor(String name, Map<String, Object> config) {
        super(name, config);
    }

    public InlineMacroProcessor(String name) {
        this(name, null);
    }

    public InlineMacroProcessor() {
        this(null, null);
    }

    /**
     * Lookup the regexp option, resolving it first if necessary. Once this method is called, the regexp is considered
     * frozen.
     */
    public Pattern getRegexp() {
        Object regexp = config.get("regexp");
        if (regexp instanceof Pattern pattern) {
            return pattern;
        } else if (regexp instanceof String str) {
            Pattern pattern = Rx.compile(str);
            config.put("regexp", pattern);
            return pattern;
        }
        Pattern pattern = resolveRegexp(name, config.get("format") == null ? null : config.get("format").toString());
        config.put("regexp", pattern);
        return pattern;
    }

    public Pattern resolveRegexp(String name, String format) {
        if (!Rx.matches(Rx.MACRO_NAME_RX, name == null ? "" : name)) {
            throw new IllegalArgumentException("invalid name for inline macro: " + name);
        }
        return RX_CACHE.computeIfAbsent(name + "|" + format, key -> Rx.compile("\\\\?" + name + ":"
                + ("short".equals(format) ? "(){0}" : "(\\S+?)") + "\\[(|" + Asciidoctor.CC_ANY + "*?[^\\\\])\\]"));
    }

    public Object process(AbstractNode parent, String target, Map<String, Object> attributes) {
        if (processFunction == null) {
            throw new UnsupportedOperationException("InlineMacroProcessor subclass " + getClass().getName()
                    + " must implement the process method");
        }
        return processFunction.process(parent, target, attributes);
    }

    /**
     * Defines the process method of this processor (DSL).
     */
    public InlineMacroProcessor process(ProcessFunction function) {
        this.processFunction = function;
        return this;
    }

    public boolean isProcessBlockGiven() {
        return processFunction != null;
    }

    /**
     * Sets the format of the macro (short or longhand) (DSL).
     */
    public InlineMacroProcessor format(String value) {
        option("format", value);
        return this;
    }

    public InlineMacroProcessor matchFormat(String value) {
        return format(value);
    }

    public InlineMacroProcessor usingFormat(String value) {
        return format(value);
    }

    /**
     * Sets the regular expression used to match the macro (DSL).
     */
    public InlineMacroProcessor match(Pattern value) {
        option("regexp", value);
        return this;
    }
}
