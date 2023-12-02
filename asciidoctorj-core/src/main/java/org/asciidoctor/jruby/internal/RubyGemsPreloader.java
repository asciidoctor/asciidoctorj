package org.asciidoctor.jruby.internal;

import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.jruby.Ruby;
import org.jruby.RubyHash;

import java.util.Map;

public class RubyGemsPreloader {

    private static final String CODERAY = "coderay";
    private static final String ERUBIS = "erubis";
    private static final String EPUB3 = "epub3";
    private static final String PDF = "pdf";
    private static final String REVEALJS = "asciidoctor-revealjs";

    private static final Map<String, String> optionToRequiredGem = Map.of(
            Options.ERUBY, "require 'erubis'",
            Options.TEMPLATE_DIRS, "require 'tilt'",
            Attributes.CACHE_URI, "require 'open-uri/cached'",
            Attributes.DATA_URI, "require 'base64'",
            Attributes.SOURCE_HIGHLIGHTER, "require 'coderay'",
            EPUB3, "require 'asciidoctor-epub3'",
            PDF, "require 'asciidoctor-pdf'",
            REVEALJS, "require 'asciidoctor-revealjs'"
    );

    private final Ruby rubyRuntime;

    public RubyGemsPreloader(Ruby rubyRuntime) {
        this.rubyRuntime = rubyRuntime;
    }

    public void preloadRequiredLibraries(Map<String, Object> options) {

        if (options.containsKey(Options.ATTRIBUTES)) {
            Map<String, Object> attributes = (Map<String, Object>) options.get(Options.ATTRIBUTES);

            if (isOptionSet(attributes, Attributes.SOURCE_HIGHLIGHTER)
                    && isOptionWithValue(attributes, Attributes.SOURCE_HIGHLIGHTER, CODERAY)) {
                preloadLibrary(Attributes.SOURCE_HIGHLIGHTER);
            }

            if (isOptionSet(attributes, Attributes.CACHE_URI)) {
                preloadLibrary(Attributes.CACHE_URI);
            }

            if (isOptionSet(attributes, Attributes.DATA_URI)) {
                preloadLibrary(Attributes.DATA_URI);
            }
        }

        if (isOptionSet(options, Options.ERUBY) && isOptionWithValue(options, Options.ERUBY, ERUBIS)) {
            preloadLibrary(Options.ERUBY);
        }

        if (isOptionSet(options, Options.TEMPLATE_DIRS)) {
            preloadLibrary(Options.TEMPLATE_DIRS);
        }

        if (isOptionSet(options, Options.BACKEND) && "epub3".equalsIgnoreCase((String) options.get(Options.BACKEND))) {
            preloadLibrary(EPUB3);
        }

        if (isOptionSet(options, Options.BACKEND) && "pdf".equalsIgnoreCase((String) options.get(Options.BACKEND))) {
            preloadLibrary(PDF);
        }

        if (isOptionSet(options, Options.BACKEND) && "revealjs".equalsIgnoreCase((String) options.get(Options.BACKEND))) {
            preloadLibrary(REVEALJS);
        }
    }

    public void preloadRequiredLibrariesCommandLine(RubyHash opts) {
        Ruby ruby = opts.getRuntime();
        if (opts.containsKey(ruby.newSymbol(Options.ATTRIBUTES))) {
            Map<String, Object> attributes = (Map<String, Object>) opts.get(ruby.newSymbol(Options.ATTRIBUTES));

            if (CODERAY.equals(attributes.get(Attributes.SOURCE_HIGHLIGHTER))) {
                preloadLibrary(Attributes.SOURCE_HIGHLIGHTER);
            }

            if (attributes.containsKey(Attributes.CACHE_URI)) {
                preloadLibrary(Attributes.CACHE_URI);
            }

            if (attributes.containsKey(Attributes.DATA_URI)) {
                preloadLibrary(Attributes.DATA_URI);
            }
            if ("epub3".equals(attributes.get(Options.BACKEND))) {
                preloadLibrary(EPUB3);
            }
            if ("pdf".equals(attributes.get(Options.BACKEND))) {
                preloadLibrary(PDF);
            }
            if ("revealjs".equals(attributes.get(Options.BACKEND))) {
                preloadLibrary(REVEALJS);
            }
        }

        if (ERUBIS.equals(opts.get(ruby.newSymbol(Options.ERUBY)))) {
            preloadLibrary(Options.ERUBY);
        }

        if (opts.containsKey(ruby.newSymbol(Options.TEMPLATE_DIRS))) {
            preloadLibrary(Options.TEMPLATE_DIRS);
        }

    }

    private void preloadLibrary(String option) {
        this.rubyRuntime.evalScriptlet(optionToRequiredGem.get(option));
    }

    private boolean isOptionWithValue(Map<String, Object> attributes, String attribute, String value) {
        return value.equals(attributes.get(attribute));
    }

    private boolean isOptionSet(Map<String, Object> attributes, String attribute) {
        return attributes.containsKey(attribute);
    }

}
