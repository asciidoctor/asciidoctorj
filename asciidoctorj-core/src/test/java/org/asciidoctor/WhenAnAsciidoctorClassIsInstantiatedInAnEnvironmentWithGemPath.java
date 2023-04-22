package org.asciidoctor;

import org.asciidoctor.categories.Polluted;
import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.jruby.internal.JRubyRuntimeContext;
import org.jruby.Ruby;
import org.jruby.RubyString;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * This test checks if the Ruby instance for an Asciidoctor instance is
 * not affected by a GEM_HOME or GEM_PATH environment variable.
 * It can mix up the JRuby instance when it can see gems from a C-Ruby
 * with native extensions.
 */
@Polluted
public class WhenAnAsciidoctorClassIsInstantiatedInAnEnvironmentWithGemPath {

    @Test
    public void should_not_have_gempath_in_ruby_env_when_created_with_null_gempath() {
        // Given: Our environment is polluted (Cannot set these env vars here, so just check that gradle has set them correctly)
        assertThat(System.getenv("GEM_PATH"), notNullValue());
        assertThat(System.getenv("GEM_HOME"), notNullValue());

        // When: A new Asciidoctor instance is created passing in a null GEM_PATH
        Asciidoctor asciidoctor = AsciidoctorJRuby.Factory.create((String) null);

        // Then: The org.jruby.JRuby instance does not see this variable
        Ruby rubyRuntime = JRubyRuntimeContext.get(asciidoctor);
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_PATH']"), is(rubyRuntime.getNil()));
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_HOME']"), is(rubyRuntime.getNil()));
    }

    @Test
    public void should_have_gempath_in_ruby_env_when_created_with_default_create() {
        // Given: Our environment is polluted (Cannot set these env vars here, so just check that gradle has set them correctly)
        assertThat(System.getenv("GEM_PATH"), notNullValue());
        assertThat(System.getenv("GEM_HOME"), notNullValue());

        // When: A new Asciidoctor instance is created passing in no GEM_PATH
        Asciidoctor asciidoctor = AsciidoctorJRuby.Factory.create();

        // Then: The org.jruby.JRuby instance sees this variable
        Ruby rubyRuntime = JRubyRuntimeContext.get(asciidoctor);
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_PATH']"), is(rubyRuntime.getNil()));
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_HOME']"), is(rubyRuntime.getNil()));
    }

    @Test
    public void should_have_gempath_in_ruby_env_when_created_with_gempath() {
        // Given: Our environment is polluted (Cannot set these env vars here, so just check that gradle has set them correctly)
        final String gemPath = "/another/path";
        assertThat(System.getenv("GEM_PATH"), notNullValue());
        assertThat(System.getenv("GEM_HOME"), notNullValue());

        // When: A new Asciidoctor instance is created passing in a null GEM_PATH
        Asciidoctor asciidoctor = AsciidoctorJRuby.Factory.create(gemPath);

        // Then: The org.jruby.JRuby instance does not see this variable
        Ruby rubyRuntime = JRubyRuntimeContext.get(asciidoctor);
        RubyString rubyGemPath = rubyRuntime.newString(gemPath);
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_PATH']"), is((Object) rubyGemPath));
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_HOME']"), is((Object) rubyGemPath));
    }

}
