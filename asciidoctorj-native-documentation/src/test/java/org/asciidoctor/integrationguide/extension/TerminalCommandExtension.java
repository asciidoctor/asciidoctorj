package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;

//tag::include-extension-registry[]
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

public class TerminalCommandExtension implements ExtensionRegistry { // <1>
  @Override
  public void register(Asciidoctor asciidoctor) { // <2>
    JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
    javaExtensionRegistry.treeprocessor(TerminalCommandTreeprocessor.class); // <3>
  }
}
//end::include-extension-registry[]
