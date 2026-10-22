package org.asciidoctor;

import org.asciidoctor.categories.Polluted;
import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.jruby.internal.JRubyRuntimeContext;
import org.jruby.Ruby;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * This test checks if the Ruby instance for an Asciidoctor instance is
 * not affected by a GEM_HOME or GEM_PATH environment variable.
 * It can mix up the JRuby instance when it can see gems from a C-Ruby
 * with native extensions.
 */
@Polluted
public class WhenAnAsciidoctorClassIsInstantiatedInAnEnvironmentWithGemPath {

    @ParameterizedTest(name = "should_not_have_gempath_in_ruby_env_when_created_with_{1}")
    @MethodSource("emptyGempathAsciidoctorProvider")
    public void should_not_have_gempath_in_ruby_env_when_created_with_(Asciidoctor asciidoctor, String testDescription) {
        // Given: Our environment is polluted (Cannot set these env vars here, so just check that gradle has set them correctly)
        assertThat(System.getenv("GEM_PATH")).isNotEmpty();
        assertThat(System.getenv("GEM_HOME")).isNotEmpty();

        // Then: The org.jruby.JRuby instance does not see this variable
        Ruby rubyRuntime = JRubyRuntimeContext.get(asciidoctor);
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_PATH']")).isEqualTo(rubyRuntime.getNil());
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_HOME']")).isEqualTo(rubyRuntime.getNil());
    }

    private static Stream<Arguments> emptyGempathAsciidoctorProvider() {
        return Stream.of(
                arguments(AsciidoctorJRuby.Factory.create(), "default_create"),
                arguments(AsciidoctorJRuby.Factory.create((String) null), "null_gempath")
        );
    }

    @ParameterizedTest(name = "should_have_gempath_in_ruby_env_when_created_with_{3}")
    @MethodSource("modifiedGempathAsciidoctorProvider")
    public void should_have_gempath_in_ruby_env_when_created_with_(Asciidoctor asciidoctor,
                                                                   String expectedGemPath,
                                                                   String expectedGemHome,
                                                                   String testDescription) {
        // Given: Our environment is polluted (Cannot set these env vars here, so just check that gradle has set them correctly)
        assertThat(System.getenv("GEM_PATH")).isNotEmpty();
        assertThat(System.getenv("GEM_HOME")).isNotEmpty();

        // Then: The org.jruby.JRuby instance does not see this variable
        Ruby rubyRuntime = JRubyRuntimeContext.get(asciidoctor);
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_PATH']").asJavaString()).isEqualTo(expectedGemPath);
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_HOME']").asJavaString()).isEqualTo(expectedGemHome);
    }

    private static Stream<Arguments> modifiedGempathAsciidoctorProvider() {
        final ClassLoader customClassloader = new String().getClass().getClassLoader();
        final String customGemPath = "/another/gempath";
        return Stream.of(
                arguments(AsciidoctorJRuby.Factory.create(customGemPath),
                        customGemPath, customGemPath, "gempath"),
                arguments(AsciidoctorJRuby.Factory.create(customClassloader),
                        Polluted.GEM_PATH, Polluted.GEM_HOME, "classloader"),
                arguments(AsciidoctorJRuby.Factory.create(customClassloader, customGemPath),
                        customGemPath, customGemPath, "classloader_and_custom_gempath")
        );
    }

    @Test
    public void should_have_gempath_in_ruby_env_when_created_with_custom_paths() {
        final String[] loadPaths = {"/load/path1", "/load/path2"};
        final String customGemPath = "/another/custom/gempath";
        // Given: Our environment is polluted (Cannot set these env vars here, so just check that gradle has set them correctly)
        assertThat(System.getenv("GEM_PATH")).isNotEmpty();
        assertThat(System.getenv("GEM_HOME")).isNotEmpty();

        Asciidoctor asciidoctor = AsciidoctorJRuby.Factory.create(List.of(loadPaths), customGemPath);

        // Then: The org.jruby.JRuby instance does not see this variable
        Ruby rubyRuntime = JRubyRuntimeContext.get(asciidoctor);
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_PATH']").asJavaString()).isEqualTo(customGemPath);
        assertThat(rubyRuntime.evalScriptlet("ENV['GEM_HOME']").asJavaString()).isEqualTo(customGemPath);
        assertThat(getLoadPaths(rubyRuntime)).contains(loadPaths);
    }

    private static List<String> getLoadPaths(Ruby rubyRuntime) {
        return (List<String>) rubyRuntime.getLoadService().getLoadPath()
                .convertToArray()
                .stream()
                .map(v -> v.toString())
                .collect(Collectors.toList());
    }
}
