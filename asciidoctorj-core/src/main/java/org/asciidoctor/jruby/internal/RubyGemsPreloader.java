package org.asciidoctor.jruby.internal;

import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.jruby.Ruby;

import java.util.Map;
import java.util.Optional;

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

    /**
     * Preload required libraries based on the options passed to the AsciidoctorJ API.
     * This method should only be used if a document is converted via the AsciidoctorJ API.
     * @param options The map with options that will be passed to the AsciidoctorJ API.
     *                This is usually obtained by calling Options.map() on an Options instance.
     *                The attributes are passed as a nested Map with String keys and String values.
     */
    public void preloadRequiredLibraries(Map<? super String, Object> options) {

        Map<Object, Object> opts = RubyHashUtil.convertRubyHashMapToMap(options);
        Map<String, Object> attributes = (Map<String, Object>) opts.get(Options.ATTRIBUTES);
        if (attributes != null) {
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

        if (isOptionSet(opts, Options.ERUBY) && isOptionWithValue(opts, Options.ERUBY, ERUBIS)) {
            preloadLibrary(Options.ERUBY);
        }

        if (isOptionSet(opts, Options.TEMPLATE_DIRS)) {
            preloadLibrary(Options.TEMPLATE_DIRS);
        }

        Optional.ofNullable(opts.get(Options.BACKEND))
                .or(() -> Optional.ofNullable(attributes).map(a -> a.get(Attributes.BACKEND)))
                .map(Object::toString)
                .ifPresent(backend -> {
                    if ("epub3".equalsIgnoreCase(backend)) {
                        preloadLibrary(EPUB3);
                    } else if ("pdf".equalsIgnoreCase(backend)) {
                        preloadLibrary(PDF);
                    } else if ("revealjs".equalsIgnoreCase(backend)) {
                        preloadLibrary(REVEALJS);
                    }
                });
    }

    private void preloadLibrary(String option) {
        this.rubyRuntime.evalScriptlet(optionToRequiredGem.get(option));
    }

    private boolean isOptionWithValue(Map<? super String, Object> attributes, String attribute, String value) {
        return value.equals(attributes.get(attribute));
    }

    private boolean isOptionSet(Map<? super String, Object> attributes, String attribute) {
        return attributes.containsKey(attribute);
    }

}
