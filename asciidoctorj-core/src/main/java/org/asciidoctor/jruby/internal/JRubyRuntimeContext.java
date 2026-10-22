package org.asciidoctor.jruby.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.jruby.ast.impl.ContentNodeImpl;
import org.jruby.Ruby;
import org.jruby.java.proxies.RubyObjectHolderProxy;
import org.jruby.runtime.builtin.IRubyObject;

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

    public static Ruby get(ContentNode node) {
        if (node instanceof IRubyObject) {
            return ((IRubyObject) node).getRuntime();
        } else if (node instanceof RubyObjectHolderProxy) {
            return ((RubyObjectHolderProxy) node).__ruby_object().getRuntime();
        } else if (node instanceof ContentNodeImpl) {
            IRubyObject nodeDelegate = ((ContentNodeImpl) node).getRubyObject();
            return nodeDelegate.getRuntime();
        } else {
            throw new IllegalArgumentException("Don't know what to with a " + node);
        }
    }

}
