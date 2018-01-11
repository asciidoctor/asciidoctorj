package org.asciidoctor.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import unusual.extension.BoldifyPostProcessor;

@RunWith(Arquillian.class)
public class WhenLoadingExtensionFromUnusualPackage {

  @ArquillianResource
  private ClasspathResources classpath;

  @ArquillianResource(Unshared.class)
  private Asciidoctor asciidoctor;

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
