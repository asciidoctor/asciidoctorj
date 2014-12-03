package org.asciidoctor.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Map;

import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.jruby.RubyBoolean;
import org.junit.Test;
import org.junit.Ignore;

public class WhenDocumentIsRenderedWithPreloading {

    @Test
    public void coderay_gem_should_be_preloaded() {

        Map<String, Object> options = OptionsBuilder.options()
                .attributes(AttributesBuilder.attributes().sourceHighlighter("coderay").get()).asMap();

        JRubyAsciidoctor asciidoctor = (JRubyAsciidoctor) JRubyAsciidoctor.create();

        asciidoctor.rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) asciidoctor.rubyRuntime.evalScriptlet("require 'coderay'");
        assertThat(evalScriptlet.isFalse(), is(true));

    }

    @Test
    public void not_coderay_gem_should_not_be_preloaded() {

        Map<String, Object> options = OptionsBuilder.options()
                .attributes(AttributesBuilder.attributes().sourceHighlighter("pygments").get()).asMap();

        JRubyAsciidoctor asciidoctor = (JRubyAsciidoctor) JRubyAsciidoctor.create();

        asciidoctor.rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) asciidoctor.rubyRuntime.evalScriptlet("require 'coderay'");
        assertThat(evalScriptlet.isTrue(), is(true));

    }

    @Test
    public void erubis_gem_should_be_preloaded() {

        Map<String, Object> options = OptionsBuilder.options().eruby("erubis").asMap();

        JRubyAsciidoctor asciidoctor = (JRubyAsciidoctor) JRubyAsciidoctor.create();

        asciidoctor.rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) asciidoctor.rubyRuntime.evalScriptlet("require 'erubis'");
        assertThat(evalScriptlet.isFalse(), is(true));

    }

    @Test
    public void not_erubis_gem_should_be_preloaded() {

        Map<String, Object> options = OptionsBuilder.options().eruby("erb").asMap();

        JRubyAsciidoctor asciidoctor = (JRubyAsciidoctor) JRubyAsciidoctor.create();

        asciidoctor.rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) asciidoctor.rubyRuntime.evalScriptlet("require 'erubis'");
        assertThat(evalScriptlet.isTrue(), is(true));

    }

    @Test
    public void template_dir_should_preload_tilt() {

        Map<String, Object> options = OptionsBuilder.options().templateDir(new File(".")).asMap();

        JRubyAsciidoctor asciidoctor = (JRubyAsciidoctor) JRubyAsciidoctor.create();

        asciidoctor.rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) asciidoctor.rubyRuntime.evalScriptlet("require 'tilt'");
        assertThat(evalScriptlet.isFalse(), is(true));

    }

    @Test
    public void data_uri_gem_should_be_preloaded() {

        Map<String, Object> options = OptionsBuilder.options()
                .attributes(AttributesBuilder.attributes().dataUri(true).get()).asMap();

        JRubyAsciidoctor asciidoctor = (JRubyAsciidoctor) JRubyAsciidoctor.create();

        asciidoctor.rubyGemsPreloader.preloadRequiredLibraries(options);
        RubyBoolean evalScriptlet = (RubyBoolean) asciidoctor.rubyRuntime.evalScriptlet("require 'base64'");
        assertThat(evalScriptlet.isFalse(), is(true));

    }

}
