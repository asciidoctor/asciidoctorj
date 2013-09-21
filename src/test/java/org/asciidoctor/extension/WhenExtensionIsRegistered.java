package org.asciidoctor.extension;

import static org.junit.Assert.assertThat;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.IOException;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.Placement;
import org.asciidoctor.SafeMode;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WhenExtensionIsRegistered {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

    @Test
    public void a_preprocessor_should_be_executed_before_document_is_rendered() {
        asciidoctor.preprocessor(FrontMatterPreprocessorExtension.class);

        String content = asciidoctor.renderFile(new File(
                "target/test-classes/render-with-front-matter.adoc"),
                new Options());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class",
                "content").first();
        assertThat(contentElement.text(), is("---\n"
                + "[tags: [announcement, website]]\n" + "---"));

    }
    
    @Test
    public void a_postprocessor_should_be_executed_after_document_is_rendered() throws IOException {
        asciidoctor.postprocessor(CustomFooterPostProcessor.class);

        Options options = options().inPlace(false)
                .toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();
        
       asciidoctor.renderFile(new File(
                "target/test-classes/rendersample.asciidoc"),
                options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        Document doc = Jsoup.parse(renderedFile, "UTF-8");
        
        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), is("Last updated 2013-09-21 09:25:45 CEST " + 
        		"Copyright Acme, Inc."));
    }

    @Test
    public void a_include_processor_should_be_executed_when_include_macro_is_found() {
        asciidoctor.includeProcessor(UriIncludeProcessor.class);

        String content = asciidoctor.renderFile(new File(
                "target/test-classes/sample-with-uri-include.ad"),
                new Options());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class",
                "ruby language-ruby").first();

        assertThat(contentElement.text(), is("source 'https://rubygems.org'gemspec# enable this group to use Guard for continuous testing# after removing comments, run `bundle install` then `guard` #group :guardtest do#  gem 'guard'#  gem 'guard-test'#  gem 'libnotify'#  gem 'listen', :github => 'guard/listen'#end"));

    }
    
    //@Test
    public void a_block_processor_should_be_executed_when_registered_block_is_found_in_document() throws IOException {
        asciidoctor.block("yell", YellBlock.class);

        String content = asciidoctor.renderFile(new File(
                "target/test-classes/sample-with-yell-block.ad"),
                new Options());
        
        System.out.println(content);
    }
    
}
