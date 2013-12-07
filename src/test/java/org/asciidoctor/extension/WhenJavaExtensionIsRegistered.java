package org.asciidoctor.extension;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WhenJavaExtensionIsRegistered {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

    @Test
    public void a_preprocessor_should_be_executed_before_document_is_rendered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor
                .javaExtensionRegistry();

        javaExtensionRegistry
                .preprocessor(FrontMatterPreprocessorExtension.class);

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
    public void a_postprocessor_should_be_executed_after_document_is_rendered()
            throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor
                .javaExtensionRegistry();

        javaExtensionRegistry.postprocessor(CustomFooterPostProcessor.class);

        Options options = options().inPlace(false)
                .toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.renderFile(new File(
                "target/test-classes/rendersample.asciidoc"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_include_processor_should_be_executed_when_include_macro_is_found() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor
                .javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(UriIncludeProcessor.class);

        String content = asciidoctor.renderFile(new File(
                "target/test-classes/sample-with-uri-include.ad"),
                new Options());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class",
                "ruby language-ruby").first();

        assertThat(contentElement.text(),
                startsWith("source 'https://rubygems.org"));

    }

    @Test
    public void a_treeprocessor_should_be_executed_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor
                .javaExtensionRegistry();

        javaExtensionRegistry.treeprocessor(TerminalCommandTreeprocessor.class);

        String content = asciidoctor.renderFile(new File(
                "target/test-classes/sample-with-terminal-command.ad"),
                new Options());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class",
                "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command")
                .last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

    }

    @Test
    public void extensions_should_be_correctly_added_using_extension_registry()
            throws IOException {

        // To avoid registering the same extension over and over for all tests,
        // service is instantiated manually.
        new ArrowsAndBoxesExtension().register(asciidoctor);

        Options options = options().inPlace(false)
                .toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.renderFile(new File(
                "target/test-classes/arrows-and-boxes-example.ad"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element arrowsJs = doc
                .select("script[src=http://www.headjump.de/javascripts/arrowsandboxes.js")
                .first();
        assertThat(arrowsJs, is(notNullValue()));

        Element arrowsCss = doc
                .select("link[href=http://www.headjump.de/stylesheets/arrowsandboxes.css")
                .first();
        assertThat(arrowsCss, is(notNullValue()));

        Element arrowsAndBoxes = doc.select("pre[class=arrows-and-boxes")
                .first();
        assertThat(arrowsAndBoxes, is(notNullValue()));

    }

    @Test
    public void a_block_macro_extension_should_be_executed_when_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor
                .javaExtensionRegistry();

        javaExtensionRegistry.blockMacro("gist", GistMacro.class);

        String content = asciidoctor
                .renderFile(new File(
                        "target/test-classes/sample-with-gist-macro.ad"),
                        new Options());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void an_inline_macro_extension_should_be_executed_when_an_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor
                .javaExtensionRegistry();

        javaExtensionRegistry.inlineMacro("man", ManpageMacro.class);

        String content = asciidoctor.renderFile(new File(
                "target/test-classes/sample-with-man-link.ad"), new Options());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertThat(link.attr("href"), is("gittutorial.html"));

    }

    @Test
    public void should_unregister_all_current_registered_extensions()
            throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor
                .javaExtensionRegistry();

        javaExtensionRegistry.postprocessor(CustomFooterPostProcessor.class);

        Options options = options().inPlace(false)
                .toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();
        
        asciidoctor.unregisterAllExtensions();
        asciidoctor.renderFile(new File(
                "target/test-classes/rendersample.asciidoc"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), not(containsString("Copyright Acme, Inc.")));
    }

    @Test
    @Ignore("Ignored because of bug http://discuss.asciidoctor.org/Problem-registering-two-times-block-extension-AsciidoctorJ-td898.html")
    public void a_block_processor_should_be_executed_when_registered_block_is_found_in_document()
            throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor
                .javaExtensionRegistry();
        javaExtensionRegistry.block("yell", YellBlock.class);

        String content = asciidoctor
                .renderFile(new File(
                        "target/test-classes/sample-with-yell-block.ad"),
                        new Options());
        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(),
                is("THE TIME IS NOW. GET A MOVE ON."));

    }

}
