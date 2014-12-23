package org.asciidoctor.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Block;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.util.ClasspathResources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import unusual.extension.BoldifyPostProcessor;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import static org.asciidoctor.OptionsBuilder.options;

public class WhenLoadingExtensionFromUnusualPackage {

  @Rule
  public ClasspathResources classpath = new ClasspathResources();

  private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

  @Test
  public void shouldAllowLoadingUsingInstance() {
    final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
    registry.postprocessor(new unusual.extension.BoldifyPostProcessor());
  }

  @Test
  public void shouldAllowLoadingByClassName() {
    final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
    registry.postprocessor(BoldifyPostProcessor.class.getCanonicalName());
  }

  @Test
  public void shouldAllowLoadingByClass() {
    final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
    registry.postprocessor(BoldifyPostProcessor.class);
  }

}
