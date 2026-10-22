package org.asciidoctor.jruby.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.jruby.RubyBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.Map;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ExtendWith(AsciidoctorExtension.class)
public class WhenDocumentIsRenderedWithPreloading {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @Test
    public void coderay_gem_should_be_preloaded() {

        Map<String, Object> options = Options.builder()
                .attributes(Attributes.builder().sourceHighlighter("coderay").build())
                .build()
                .map();

        ((JRubyAsciidoctor) asciidoctor).rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) ((JRubyAsciidoctor) asciidoctor).rubyRuntime.evalScriptlet("require 'coderay'");
        assertThat(evalScriptlet.isFalse(), is(true));
    }

    @Test
    public void not_coderay_gem_should_not_be_preloaded() {

        Map<String, Object> options = Options.builder()
                .attributes(Attributes.builder().sourceHighlighter("pygments").build())
                .build()
                .map();

        ((JRubyAsciidoctor) asciidoctor).rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) ((JRubyAsciidoctor) asciidoctor).rubyRuntime.evalScriptlet("require 'coderay'");
        assertThat(evalScriptlet.isTrue(), is(true));
    }

    @Test
    public void erubis_gem_should_be_preloaded() {

        Map<String, Object> options = Options.builder()
                .eruby("erubis")
                .build()
                .map();

        ((JRubyAsciidoctor) asciidoctor).rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) ((JRubyAsciidoctor) asciidoctor).rubyRuntime.evalScriptlet("require 'erubis'");
        assertThat(evalScriptlet.isFalse(), is(true));
    }

    @Test
    public void not_erubis_gem_should_be_preloaded() {

        Map<String, Object> options = Options.builder()
                .eruby("erb")
                .build()
                .map();

        ((JRubyAsciidoctor) asciidoctor).rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) ((JRubyAsciidoctor) asciidoctor).rubyRuntime.evalScriptlet("require 'erubis'");
        assertThat(evalScriptlet.isTrue(), is(true));
    }

    @Test
    public void template_dir_should_preload_tilt() {

        Map<String, Object> options = Options.builder()
                .templateDirs(new File("."))
                .build()
                .map();

        ((JRubyAsciidoctor) asciidoctor).rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) ((JRubyAsciidoctor) asciidoctor).rubyRuntime.evalScriptlet("require 'tilt'");
        assertThat(evalScriptlet.isFalse(), is(true));
    }

    @Test
    public void data_uri_gem_should_be_preloaded() {

        Map<String, Object> options = Options.builder()
                .attributes(Attributes.builder().dataUri(true).build())
                .build()
                .map();

        ((JRubyAsciidoctor) asciidoctor).rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) ((JRubyAsciidoctor) asciidoctor).rubyRuntime.evalScriptlet("require 'base64'");
        assertThat(evalScriptlet.isFalse(), is(true));
    }
}
