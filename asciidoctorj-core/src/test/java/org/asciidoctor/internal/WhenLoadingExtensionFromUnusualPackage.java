package org.asciidoctor.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.util.ClasspathResources;
import org.junit.Rule;
import org.junit.Test;
import unusual.extension.BoldifyPostProcessor;

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

  @Test
  public void shouldAllowTreeprocessorLoadingUsingInstance() {
    final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
    registry.treeprocessor(new unusual.extension.UnusualTreeProcessor());
  }

  @Test
  public void shouldAllowTreeprocessorLoadingByClassName() {
    final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
    registry.treeprocessor(unusual.extension.UnusualTreeProcessor.class.getCanonicalName());
  }

  @Test
  public void shouldAllowTreeprocessorLoadingByClass() {
    final JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
    registry.treeprocessor(unusual.extension.UnusualTreeProcessor.class);
  }

}
