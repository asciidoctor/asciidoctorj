package org.asciidoctor.internal;

import org.jruby.Ruby;

/**
 * This class is a bit complicated to explain because we require to have access
 * to Ruby runtime in places where it is impossible to retrieve it from
 * JRubyAscidoctor class because it is Asciidoctor Ruby part which instantiate
 * an extension. Moreover the approach used before using this class, it makes
 * Gradle crash.
 * 
 * @author alex
 * 
 */
public class JRubyRuntimeContext {

    private static Ruby rubyRuntime = null;

    private JRubyRuntimeContext() {
    }

    static void set(Ruby ruby) {
        rubyRuntime = ruby;
    }

    public static Ruby get() {
        if (rubyRuntime == null) {
            throw new IllegalStateException(
                    "Ruby runtime should be set, was not present in current Thread instance.");
        }
        return rubyRuntime;
    }

}
