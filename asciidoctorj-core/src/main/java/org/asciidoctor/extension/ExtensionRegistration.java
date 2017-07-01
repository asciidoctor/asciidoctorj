package org.asciidoctor.extension;

import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubySymbol;

import java.util.UUID;

/**
 * This object represents a registration of a single Extension.
 * It can be used to unregister an extension at an Asciidoctor instance again.
 *
 * <p>Example:</p>
 * <pre><code>
 * Asciidoctor asciidoctor = ...
 * ExtensionRegistration registration =
 *     asciidoctor.javaExtensionRegistry().docInfoProcessor(...);
 * asciidoctor.convert(...); // Convert with the docInfoProcessor enabled.
 * asciidoctor.unregisterExtension(registration);
 * asciidoctor.convert(...); // Convert without the docInfoProcessor.
 * </code></pre>
 */
public class ExtensionRegistration {

  private String name;

  public ExtensionRegistration() {
    this.name = UUID.randomUUID().toString();
  }

  public RubySymbol getName(Ruby rubyInstance) {
    return RubyUtils.toSymbol(rubyInstance, name);
  }
}
