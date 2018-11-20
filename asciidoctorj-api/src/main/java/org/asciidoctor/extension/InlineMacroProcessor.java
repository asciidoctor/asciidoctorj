package org.asciidoctor.extension;

import org.asciidoctor.ast.ContentNode;

import java.util.HashMap;
import java.util.Map;

public abstract class InlineMacroProcessor extends MacroProcessor<ContentNode> {

    /**
     * This value is used as the config option key when defining a regular expression that should be
     * used to match an inline macro invocation.
     * Its value must be a String containing a regular expression :
     *
     * <p>Example to make a InlineMacroProcessor work on inline macros of the form {@code man:[A-Za-z0-9]+\\[.*?\\]}:
     * <pre>
     * <verbatim>
     * Map&lt;String, Object&gt; config = new HashMap&lt;&gt;();
     * config.put(REGEXP, "man:([A-Za-z0-9]+)\\[(.*?)\\]");
     * InlineMacroProcessor inlineMacroProcessor = new InlineMacroProcessor("man", config);
     * asciidoctor.javaExtensionRegistry().inlineMacro(inlineMacroProcessor);
     * </verbatim>
     * </pre>
     * </p>
     * <p>Note the parens {@code (} and {@code )} in the regular expression in the example to capture
     * the target and attributes of the macro invocation.</p>
     */
    public static final String REGEXP = "regexp";

    public InlineMacroProcessor() {
        this(null);
    }

    public InlineMacroProcessor(String macroName) {
        this(macroName, new HashMap<>());
    }

    public InlineMacroProcessor(String macroName, Map<String, Object> config) {
        super(macroName, config);
    }
}
