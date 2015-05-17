package org.asciidoctor.internal;

import org.asciidoctor.Asciidoctor;
import org.jruby.Ruby;

/**
 * This class gives access to the Ruby instance that is used by a JRuby Asciidoctor instance.
 *
 * @author alex
 * @author Robert
 *
 */
public class JRubyRuntimeContext {

    private JRubyRuntimeContext() {
    }

    public static Ruby get(Asciidoctor asciidoctor) {
        if (!(asciidoctor instanceof JRubyAsciidoctor)) {
            throw new IllegalArgumentException(
                    "Expected a " + JRubyAsciidoctor.class.getName() + "instead of " + asciidoctor);
        }
        return ((JRubyAsciidoctor) asciidoctor).getRubyRuntime();
    }

}
