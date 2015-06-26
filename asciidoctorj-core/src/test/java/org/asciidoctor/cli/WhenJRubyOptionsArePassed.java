package org.asciidoctor.cli;

import com.beust.jcommander.JCommander;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;

public class WhenJRubyOptionsArePassed {

    @ArquillianResource
    private ClasspathResources classpath;

    @ArquillianResource
    public TemporaryFolder temporaryFolder;

    public String pwd = new File("").getAbsolutePath();

    @Test
    public void shouldUseRubyVersion20ByDefault() {
        // Given
        AsciidoctorInvoker invoker = new AsciidoctorInvoker();
        AsciidoctorCliOptions asciidoctorCliOptions = new AsciidoctorCliOptions();
        JCommander jCommander = new JCommander(asciidoctorCliOptions);

        // When
        JRubyAsciidoctor asciidoctor = invoker.buildAsciidoctorJInstance(asciidoctorCliOptions);

        // Then
        assertThat(asciidoctor.getRubyRuntime().evalScriptlet("RUBY_VERSION").asJavaString(), is("2.0.0"));

    }

    @Test
    public void shouldUseRubyVersion193WhenConfigured() {
        // Given
        AsciidoctorInvoker invoker = new AsciidoctorInvoker();
        AsciidoctorCliOptions asciidoctorCliOptions = new AsciidoctorCliOptions();
        JCommander jCommander = new JCommander(asciidoctorCliOptions, "-R", "compile.mode=OFF", "-R", "compat.version=1.9");

        // When
        JRubyAsciidoctor asciidoctor = invoker.buildAsciidoctorJInstance(asciidoctorCliOptions);

        // Then
        assertThat(asciidoctor.getRubyRuntime().evalScriptlet("RUBY_VERSION").asJavaString(), is("1.9.3"));

    }

}
