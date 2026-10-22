package org.asciidoctor.jruby;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.RubyExtensionRegistry;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;

import java.util.List;

public interface AsciidoctorJRuby extends Asciidoctor {


  /**
   * Factory for creating a new instance of Asciidoctor interface.
   *
   * @author lordofthejars
   */
  public static final class Factory {

    private Factory() {
    }

    /**
     * Creates a new instance of Asciidoctor.
     *
     * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
     * Ruby calls.
     */
    public static AsciidoctorJRuby create() {
      return JRubyAsciidoctor.create();
    }

    /**
     * Creates a new instance of Asciidoctor and sets GEM_PATH environment
     * variable to provided gemPath. This method is mostly used in OSGi
     * environments.
     *
     * @param gemPath where gems are located.
     * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
     * Ruby calls.
     */
    public static AsciidoctorJRuby create(String gemPath) {
      return JRubyAsciidoctor.create(gemPath);
    }

    /**
     * Creates a new instance of Asciidoctor and sets loadPath to provided paths. This method is mostly used in OSGi
     * environments.
     *
     * @param loadPaths where Ruby libraries are located.
     * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
     * Ruby calls.
     */
    public static AsciidoctorJRuby create(List<String> loadPaths) {
      return JRubyAsciidoctor.create(loadPaths);
    }

    /**
     * Creates a new instance of Asciidoctor and sets a specific classloader for the  JRuby runtime to use.
     * This method is for use in environments like OSGi.
     * To initialize Asciidoctor in OSGi create the Asciidoctor instance like this:
     *
     * <pre>
     * org.jruby.javasupport.JavaEmbedUtils.initialize(Arrays.asList("uri:classloader:/gems/asciidoctor-1.5.8/lib"));
     * Asciidoctor asciidoctor = Asciidoctor.Factory.create(this.getClass().getClassLoader());
     * </pre>
     *
     * @param classloader
     * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
     *         Ruby calls.
     */
    public static AsciidoctorJRuby create(ClassLoader classloader) {
      return JRubyAsciidoctor.create(classloader);
    }

    /**
     * Creates a new instance of Asciidoctor and sets a specific classloader and gempath for the JRuby runtime to use.
     * This method is for use in environments like OSGi.
     * To initialize Asciidoctor in OSGi create the Asciidoctor instance like this:
     *
     * <pre>
     * org.jruby.javasupport.JavaEmbedUtils.initialize(Arrays.asList("uri:classloader:/gems/asciidoctor-1.5.8/lib"));
     * Asciidoctor asciidoctor = Asciidoctor.Factory.create(this.getClass().getClassLoader());
     * </pre>
     *
     * @param classloader
     * @param gemPath
     * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
     */
    public static AsciidoctorJRuby create(ClassLoader classloader, String gemPath) {
      return JRubyAsciidoctor.create(classloader, gemPath);
    }

    /**
     * Creates a new instance of Asciidoctor and sets loadPath to provided paths.
     * The gem path of the Ruby instance is set to the gemPath parameter.
     *
     * @param loadPaths where Ruby libraries are located.
     * @param gemPath   where gems are located.
     * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
     * Ruby calls.
     */
    public static AsciidoctorJRuby create(List<String> loadPaths, String gemPath) {
      return JRubyAsciidoctor.create(loadPaths, gemPath);
    }

  }


}
