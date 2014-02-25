package org.asciidoctor.extension;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.RubyRegexp;
import org.jruby.util.RegexpOptions;

import java.util.Map;

public abstract class InlineMacroProcessor extends MacroProcessor {

    protected RubyRegexp regexp;
    
    public InlineMacroProcessor(String macroName, Map<String, Object> config) {
        super(macroName, config);
        // NOTE at this point, config and this.config are different objects
        if (config.containsKey("regexp")) {
            this.regexp = convertRegexp(config.get("regexp"));
        }
        else {
            this.regexp = resolveRegexp(macroName, (String) config.get("format"));
        }
        this.config.put(RubyUtils.toSymbol(rubyRuntime, "regexp"), this.regexp);
    }

    private RubyRegexp convertRegexp(Object regexp) {
        return RubyRegexp.newRegexp(rubyRuntime, regexp.toString(), RegexpOptions.NULL_OPTIONS);
    }

    public RubyRegexp resolveRegexp(String macroName, String format) {
        String maybe = "?";
        String or = "|";
        String lit_backslash = "\\\\";
        String open_bracket = "\\[";
        String close_bracket = "\\]";
        String esc_close_bracket = lit_backslash + close_bracket;
        String not_close_bracket = "^" + close_bracket;
        String not_whitespace = "\\S+?";
        if (format == "short") {
            return convertRegexp(
                    lit_backslash + maybe +
                    macroName + ":" +
                    open_bracket +
                            "((?:" + esc_close_bracket + or + "[" + not_close_bracket + "]" + ")*?)" +
                    close_bracket);
        }
        else {
            return convertRegexp(
                    lit_backslash + maybe +
                    macroName + ":" +
                    "(" + not_whitespace + ")" +
                    open_bracket +
                            "((?:" + esc_close_bracket + or + "[" + not_close_bracket + "]" + ")*?)" +
                    close_bracket);
        }
    }
    
    public RubyRegexp getRegexp() {
        return regexp;
    }

}
