package org.asciidoctor.extension;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.util.ClasspathResources;
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
    public ClasspathResources classpath = new ClasspathResources();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

    class RubyIncludeSource extends IncludeProcessor {

        public RubyIncludeSource(Map<String, Object> config) {
            super(config);
        }

        @Override
        public void process(DocumentRuby document, PreprocessorReader reader, String target,
                Map<String, Object> attributes) {
            StringBuilder content = readContent(target);
            reader.push_include(content.toString(), target, target, 1, attributes);
        }

        private StringBuilder readContent(String target) {
            StringBuilder content = new StringBuilder();

            try {

                URL url = new URL(target);
                InputStream openStream = url.openStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openStream));

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }

                bufferedReader.close();

            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
            return content;
        }

        @Override
        public boolean handles(String target) {
            return target.startsWith("http://") || target.startsWith("https://");
        }

    }

    @Test
    public void an_inner_class_should_be_registered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(new RubyIncludeSource(new HashMap<String, Object>()));

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-uri-include.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith("source 'https://rubygems.org"));

    }

    @Test
    public void an_inner_anonymous_class_should_be_registered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(new IncludeProcessor(new HashMap<String, Object>()) {

            @Override
            public void process(DocumentRuby documentRuby, PreprocessorReader reader, String target,
                    Map<String, Object> attributes) {
                StringBuilder content = readContent(target);
                reader.push_include(content.toString(), target, target, 1, attributes);
            }

            private StringBuilder readContent(String target) {
                StringBuilder content = new StringBuilder();

                try {

                    URL url = new URL(target);
                    InputStream openStream = url.openStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openStream));

                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        content.append(line);
                    }

                    bufferedReader.close();

                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
                return content;
            }

            @Override
            public boolean handles(String target) {
                return target.startsWith("http://") || target.startsWith("https://");
            }
        });

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-uri-include.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith("source 'https://rubygems.org"));

    }

    @Test
    public void a_docinfoprocessor_should_be_executed_and_add_meta_in_header_by_default() {
        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.docinfoProcessor(MetaRobotsDocinfoProcessor.class.getCanonicalName());

        String content = asciidoctor.renderFile(
                classpath.getResource("simple.adoc"),
                options().headerFooter(true).safe(SafeMode.SERVER).toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element metaRobots = doc.getElementsByAttributeValueContaining("name", "robots").first();
        assertThat(metaRobots, is(notNullValue()));
    }

    @Test
    public void a_docinfoprocessor_should_be_executed_and_add_meta_in_footer() {
        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("location", ":footer");
        MetaRobotsDocinfoProcessor metaRobotsDocinfoProcessor = new MetaRobotsDocinfoProcessor(options);

        javaExtensionRegistry.docinfoProcessor(metaRobotsDocinfoProcessor);

        String content = asciidoctor.renderFile(
                classpath.getResource("simple.adoc"),
                options().headerFooter(true).safe(SafeMode.SERVER).toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element footer = doc.getElementById("footer");
        // Since Asciidoctor 1.5.3 the docinfo in the footer is a sibling to the footer element
        assertTrue("robots".equals(footer.nextElementSibling().attr("name")));
    }

    @Test
    public void a_preprocessor_should_be_executed_before_document_is_rendered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(ChangeAttributeValuePreprocessor.class);

        String content = asciidoctor.renderFile(
                classpath.getResource("changeattribute.adoc"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_preprocessor_as_string_should_be_executed_before_document_is_rendered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor("org.asciidoctor.extension.ChangeAttributeValuePreprocessor");

        String content = asciidoctor.renderFile(
                classpath.getResource("changeattribute.adoc"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_preprocessor_instance_should_be_executed_before_document_is_rendered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(new ChangeAttributeValuePreprocessor(new HashMap<String, Object>()));

        String content = asciidoctor.renderFile(
                classpath.getResource("changeattribute.adoc"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_postprocessor_as_string_should_be_executed_after_document_is_rendered() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor("org.asciidoctor.extension.CustomFooterPostProcessor");

        Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.renderFile(classpath.getResource("rendersample.asciidoc"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_postprocessor_should_be_executed_after_document_is_rendered() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor(CustomFooterPostProcessor.class);

        Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.renderFile(classpath.getResource("rendersample.asciidoc"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_postprocessor_instance_should_be_executed_after_document_is_rendered() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor(new CustomFooterPostProcessor(new HashMap<String, Object>()));

        Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.renderFile(classpath.getResource("rendersample.asciidoc"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_include_processor_as_string_should_be_executed_when_include_macro_is_found() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor("org.asciidoctor.extension.UriIncludeProcessor");

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-uri-include.ad"), 
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith("source 'https://rubygems.org"));

    }

    @Test
    public void a_include_processor_should_be_executed_when_include_macro_is_found() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(UriIncludeProcessor.class);

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-uri-include.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith("source 'https://rubygems.org"));

    }

    @Test
    public void a_include_instance_processor_should_be_executed_when_include_macro_is_found() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(new UriIncludeProcessor(new HashMap<String, Object>()));

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-uri-include.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith("source 'https://rubygems.org"));

    }

    @Test
    public void a_treeprocessor_should_be_executed_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.treeprocessor(TerminalCommandTreeprocessor.class);

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-terminal-command.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

    }

    /**
     * See https://github.com/asciidoctor/asciidoctorj/issues/497.
     */
    @Test
    public void when_using_a_tree_processor_a_toc_should_still_be_created() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.treeprocessor(TerminalCommandTreeprocessor.class);

        String content = asciidoctor.renderFile(
            classpath.getResource("sample-with-sections.ad"),
            options().toFile(false)
                .attributes(AttributesBuilder.attributes().tableOfContents(true))
                .get());
    }


    @Test
    public void a_treeprocessor_as_string_should_be_executed_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.treeprocessor("org.asciidoctor.extension.TerminalCommandTreeprocessor");

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-terminal-command.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

    }

    @Test
    public void a_treeprocessor_instance_should_be_executed_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.treeprocessor(new TerminalCommandTreeprocessor(new HashMap<String, Object>()));

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-terminal-command.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

    }

    @Test
    @Ignore
    public void extensions_should_be_correctly_added_using_extension_registry() throws IOException {

        // To avoid registering the same extension over and over for all tests,
        // service is instantiated manually.
        new ArrowsAndBoxesExtension().register(asciidoctor);

        Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.renderFile(classpath.getResource("arrows-and-boxes-example.ad"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element arrowsJs = doc.select("script[src=http://www.headjump.de/javascripts/arrowsandboxes.js").first();
        assertThat(arrowsJs, is(notNullValue()));

        Element arrowsCss = doc.select("link[href=http://www.headjump.de/stylesheets/arrowsandboxes.css").first();
        assertThat(arrowsCss, is(notNullValue()));

        Element arrowsAndBoxes = doc.select("pre[class=arrows-and-boxes").first();
        assertThat(arrowsAndBoxes, is(notNullValue()));

    }

    @Test
    public void a_block_macro_extension_should_be_executed_when_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.blockMacro("gist", GistMacro.class);

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-gist-macro.ad"), 
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void a_block_macro_as_string_extension_should_be_executed_when_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.blockMacro("gist", "org.asciidoctor.extension.GistMacro");

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-gist-macro.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void a_block_macro_as_instance_extension_should_be_executed_when_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("content_model", ":raw");

        javaExtensionRegistry.blockMacro(new GistMacro("gist", options));

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-gist-macro.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void an_inline_macro_as_string_extension_should_be_executed_when_an_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.inlineMacro("man", "org.asciidoctor.extension.ManpageMacro");

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-man-link.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertThat(link.attr("href"), is("gittutorial.html"));
    }

    @Test
    public void an_inline_macro_extension_should_be_executed_when_an_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.inlineMacro("man", ManpageMacro.class);

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-man-link.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertThat(link.attr("href"), is("gittutorial.html"));

    }

    @Test
    public void an_inline_macro_as_instance_extension_should_be_executed_when_regexp_is_set_as_option_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("regexp", "man(?:page)?:(\\S+?)\\[(.*?)\\]");

        ManpageMacro inlineMacroProcessor = new ManpageMacro("man", options);
        javaExtensionRegistry.inlineMacro(inlineMacroProcessor);

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-man-link.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertNotNull(link);
        assertThat(link.attr("href"), is("gittutorial.html"));

    }

    @Test
    public void an_inline_macro_as_instance_extension_should_be_executed_when_an_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> options = new HashMap<String, Object>();

        ManpageMacro inlineMacroProcessor = new ManpageMacro("man", options);
        javaExtensionRegistry.inlineMacro(inlineMacroProcessor);

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-man-link.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertNotNull(link);
        assertThat(link.attr("href"), is("gittutorial.html"));

    }

    @Test
    public void should_unregister_all_current_registered_extensions() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor(CustomFooterPostProcessor.class);

        Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.unregisterAllExtensions();
        asciidoctor.renderFile(classpath.getResource("rendersample.asciidoc"),options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), not(containsString("Copyright Acme, Inc.")));
    }

    @Test
    public void a_block_processor_as_string_should_be_executed_when_registered_block_is_found_in_document()
            throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.block("yell", "org.asciidoctor.extension.YellStaticBlock");
        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-yell-block.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void a_block_processor_should_be_executed_when_registered_block_is_found_in_document() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.block("yell", YellStaticBlock.class);
        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-yell-block.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void a_block_processor_instance_should_be_executed_when_registered_block_is_found_in_document()
            throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("contexts", Arrays.asList(":paragraph"));
        config.put("content_model", ":simple");
        YellBlock yellBlock = new YellBlock("yell", config);
        javaExtensionRegistry.block(yellBlock);
        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-yell-block.ad"),
                options().toFile(false).get());
        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

}
