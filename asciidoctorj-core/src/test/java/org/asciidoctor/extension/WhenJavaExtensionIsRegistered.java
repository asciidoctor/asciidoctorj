package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.TestLogHandlerService;
import org.asciidoctor.util.ClasspathResources;
import org.asciidoctor.util.TestHttpServer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class WhenJavaExtensionIsRegistered {

    public static final String ASCIIDOCTORCLASS_PREFIX = "module AsciidoctorJ    include_package 'org.asciidoctor'";

    @ArquillianResource
    private ClasspathResources classpath;

    @ArquillianResource
    public TemporaryFolder testFolder;

    private Asciidoctor asciidoctor;

    @Before
    public void before() {
        asciidoctor = Asciidoctor.Factory.create();
        TestLogHandlerService.clear();
    }

    @After
    public void tearDown() throws IOException {
        if (TestHttpServer.getInstance() != null) {
            TestHttpServer.getInstance().stop();
        }
        LogManager.getLogManager().readConfiguration();
        TestLogHandlerService.clear();
    }

    class RubyIncludeSource extends IncludeProcessor {

        public RubyIncludeSource(Map<String, Object> config) {
            super(config);
        }

        @Override
        public void process(Document document, PreprocessorReader reader, String target,
                            Map<String, Object> attributes) {
            StringBuilder content = readContent(target);
            reader.push_include(content.toString(), target, target, 1, attributes);
        }

        private StringBuilder readContent(String target) {
            StringBuilder content = new StringBuilder();

            try {

                URL url = new URL(target);
                URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", TestHttpServer.getInstance().getLocalPort())));
                InputStream openStream = connection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openStream));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }

                bufferedReader.close();

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

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", classpath.getResource("org/asciidoctor/jruby/internal/asciidoctorclass.rb")));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(new RubyIncludeSource(new HashMap<>()));

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-uri-include.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));

    }

    @Test
    public void an_inner_anonymous_class_should_be_registered() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", classpath.getResource("org/asciidoctor/jruby/internal/asciidoctorclass.rb")));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(new IncludeProcessor(new HashMap<>()) {

            @Override
            public void process(Document document, PreprocessorReader reader, String target,
                                Map<String, Object> attributes) {
                StringBuilder content = readContent(target);
                reader.push_include(content.toString(), target, target, 1, attributes);
            }

            private StringBuilder readContent(String target) {
                StringBuilder content = new StringBuilder();

                try {

                    URL url = new URL(target);
                    URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", TestHttpServer.getInstance().getLocalPort())));
                    InputStream openStream = connection.getInputStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openStream));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        content.append(line);
                    }

                    bufferedReader.close();

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

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-uri-include.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));

    }

    @Test
    public void a_docinfoprocessor_should_be_executed_and_add_meta_in_header_by_default() {
        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.docinfoProcessor(MetaRobotsDocinfoProcessor.class.getCanonicalName());

        String content = asciidoctor.convertFile(
                classpath.getResource("simple.adoc"),
                options().headerFooter(true).safe(SafeMode.SERVER).toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element metaRobots = doc.getElementsByAttributeValueContaining("name", "robots").first();
        assertThat(metaRobots, is(notNullValue()));
    }

    @Test
    public void a_docinfoprocessor_should_be_executed_and_add_meta_in_footer() {
        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> options = new HashMap<>();
        options.put("location", ":footer");
        MetaRobotsDocinfoProcessor metaRobotsDocinfoProcessor = new MetaRobotsDocinfoProcessor(options);

        javaExtensionRegistry.docinfoProcessor(metaRobotsDocinfoProcessor);

        String content = asciidoctor.convertFile(
                classpath.getResource("simple.adoc"),
                options().headerFooter(true).safe(SafeMode.SERVER).toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element footer = doc.getElementById("footer");
        // Since Asciidoctor 1.5.3 the docinfo in the footer is a sibling to the footer element
        assertTrue("robots".equals(footer.nextElementSibling().attr("name")));
    }

    @Test
    public void a_preprocessor_should_be_executed_before_document_is_rendered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(ChangeAttributeValuePreprocessor.class);

        String content = asciidoctor.convertFile(
                classpath.getResource("changeattribute.adoc"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_preprocessor_as_string_should_be_executed_before_document_is_rendered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor("org.asciidoctor.extension.ChangeAttributeValuePreprocessor");

        String content = asciidoctor.convertFile(
                classpath.getResource("changeattribute.adoc"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_preprocessor_instance_should_be_executed_before_document_is_rendered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(new ChangeAttributeValuePreprocessor(new HashMap<>()));

        String content = asciidoctor.convertFile(
                classpath.getResource("changeattribute.adoc"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_postprocessor_as_string_should_be_executed_after_document_is_rendered() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor("org.asciidoctor.extension.CustomFooterPostProcessor");

        Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_postprocessor_should_be_executed_after_document_is_rendered() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor(CustomFooterPostProcessor.class);

        Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_postprocessor_instance_should_be_executed_after_document_is_rendered() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor(new CustomFooterPostProcessor(new HashMap<>()));

        Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_include_processor_as_string_should_be_executed_when_include_macro_is_found() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", classpath.getResource("org/asciidoctor/jruby/internal/asciidoctorclass.rb")));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor("org.asciidoctor.extension.UriIncludeProcessor");

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-uri-include.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));

    }

    @Test
    public void a_include_processor_should_be_executed_when_include_macro_is_found() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", classpath.getResource("org/asciidoctor/jruby/internal/asciidoctorclass.rb")));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(UriIncludeProcessor.class);

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-uri-include.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));

    }

    @Test
    public void a_include_instance_processor_should_be_executed_when_include_macro_is_found() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", classpath.getResource("org/asciidoctor/jruby/internal/asciidoctorclass.rb")));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(new UriIncludeProcessor(new HashMap<>()));

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-uri-include.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));

    }

    @Test
    public void a_include_processor_should_only_handle_its_handles() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(UriIncludeProcessor.class);

        String content = asciidoctor.convertFile(classpath.getResource("sample-with-include.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "bare").first();

        assertThat(contentElement.text(), startsWith("sample-book.adoc"));

    }

    @Test
    public void a_include_processor_can_handle_positional_attrs() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(PositionalAttrsIncludeProcessor.class);

        String content = asciidoctor.convertFile(classpath.getResource("sample-with-include-pos-attrs.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "paragraph IncludeBlock").first();

        assertThat(contentElement.text(), startsWith("My,Positional,Attribute List"));

    }

    @Test
    public void a_treeprocessor_should_be_executed_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.treeprocessor(TerminalCommandTreeprocessor.class);

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-terminal-command.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

    }

    @Test
    public void a_treeprocessor_and_blockmacroprocessor_should_be_executed_in_document() {
        this.asciidoctor.javaExtensionRegistry()
                .treeprocessor(TerminalCommandTreeprocessor.class)
                .blockMacro("gist", GistMacro.class);

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-terminal-command-and-gist-macro.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

        Element script = doc.getElementsByTag("script").first();
        assertThat(script.attr("src"), is("https://gist.github.com/42.js"));
    }

    @Test
    public void a_treeprocessor_as_string_should_be_executed_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.treeprocessor("org.asciidoctor.extension.TerminalCommandTreeprocessor");

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-terminal-command.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

    }

    @Test
    public void a_treeprocessor_instance_should_be_executed_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.treeprocessor(new TerminalCommandTreeprocessor(new HashMap<>()));

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-terminal-command.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

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

        asciidoctor.convertFile(classpath.getResource("arrows-and-boxes-example.ad"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

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

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-gist-macro.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void a_block_macro_extension_instance_should_be_executed_when_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.blockMacro(new GistMacro("gist", new HashMap<>()));

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-gist-macro.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void a_block_macro_as_string_extension_should_be_executed_when_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.blockMacro("gist", "org.asciidoctor.extension.GistMacro");

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-gist-macro.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void a_block_macro_as_instance_extension_should_be_executed_when_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> options = new HashMap<>();
        options.put(ContentModel.KEY, ContentModel.RAW);

        javaExtensionRegistry.blockMacro(new GistMacro("gist", options));

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-gist-macro.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void an_inline_macro_as_string_extension_should_be_executed_when_an_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.inlineMacro("man", "org.asciidoctor.extension.ManpageMacro");

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-man-link.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertThat(link.attr("href"), is("gittutorial.html"));

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void an_inline_macro_extension_should_be_executed_when_an_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.inlineMacro("man", ManpageMacro.class);

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-man-link.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertThat(link.attr("href"), is("gittutorial.html"));

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void an_inline_macro_as_instance_extension_should_be_executed_when_regexp_is_set_as_option_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> options = new HashMap<>();
        options.put(InlineMacroProcessor.REGEXP, "man(?:page)?:(\\S+?)\\[(.*?)\\]");

        ManpageMacro inlineMacroProcessor = new ManpageMacro("man", options);
        javaExtensionRegistry.inlineMacro(inlineMacroProcessor);

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-man-link.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertNotNull(link);
        assertThat(link.attr("href"), is("gittutorial.html"));

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void an_inline_macro_as_instance_extension_should_not_be_executed_when_regexp_is_set_and_does_not_match() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> options = new HashMap<>();
        options.put(InlineMacroProcessor.REGEXP, "man(?:page)?:(ThisDoesNotMatch)\\[(.*?)\\]");

        ManpageMacro inlineMacroProcessor = new ManpageMacro("man", options);
        javaExtensionRegistry.inlineMacro(inlineMacroProcessor);

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-man-link.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertNull(link);

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void an_inline_macro_as_instance_extension_should_be_executed_when_an_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> options = new HashMap<>();

        ManpageMacro inlineMacroProcessor = new ManpageMacro("man", options);
        javaExtensionRegistry.inlineMacro(inlineMacroProcessor);

        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-man-link.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertNotNull(link);
        assertThat(link.attr("href"), is("gittutorial.html"));

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void an_inline_macro_with_subs_should_be_executed_when_an_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.inlineMacro("say", "org.asciidoctor.extension.SayMacro");

        String adoc = "Hello say:word[]!";
        String content = asciidoctor.convert(adoc,
                options().toFile(false));

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element p = doc.getElementsByTag("p").first();
        assertThat(p, notNullValue());
        Element strong = p.getElementsByTag("strong").first();
        assertThat(strong, notNullValue());
        assertThat(strong.text(), is("word"));

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void should_unregister_all_current_registered_extensions() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor(CustomFooterPostProcessor.class);

        Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                .safe(SafeMode.UNSAFE).get();

        asciidoctor.unregisterAllExtensions();
        asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options);

        File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), not(containsString("Copyright Acme, Inc.")));
    }

    @Test
    public void a_block_processor_as_string_should_be_executed_when_registered_block_is_found_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.block("yell", "org.asciidoctor.extension.YellStaticBlock");
        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-yell-block.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void a_block_processor_should_be_executed_when_registered_block_is_found_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.block("yell", YellStaticBlock.class);
        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-yell-block.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void a_block_processor_class_should_be_executed_twice() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.block("yell", YellStaticBlock.class);
        for (int i = 0; i < 2; i++) {
            String content = asciidoctor.convertFile(
                    classpath.getResource("sample-with-yell-block.ad"),
                    options().toFile(false).get());

            org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
            Elements elements = doc.getElementsByClass("paragraph");
            assertThat(elements.size(), is(1));
            assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));
        }
    }

    @Test
    public void a_block_processor_instance_should_be_executed_when_registered_block_is_found_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> config = new HashMap<>();
        config.put(Contexts.KEY, Arrays.asList(Contexts.PARAGRAPH));
        config.put(ContentModel.KEY, ContentModel.SIMPLE);
        YellBlock yellBlock = new YellBlock("yell", config);
        javaExtensionRegistry.block(yellBlock);
        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-yell-block.ad"),
                options().toFile(false).get());
        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void a_block_processor_instance_should_be_executed_twice() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> config = new HashMap<>();
        config.put(Contexts.KEY, Arrays.asList(Contexts.PARAGRAPH));
        config.put(ContentModel.KEY, ContentModel.SIMPLE);
        YellBlock yellBlock = new YellBlock("yell", config);
        javaExtensionRegistry.block(yellBlock);

        for (int i = 0; i < 2; i++) {
            String content = asciidoctor.convertFile(
                    classpath.getResource("sample-with-yell-block.ad"),
                    options().toFile(false).get());
            org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
            Elements elements = doc.getElementsByClass("paragraph");
            assertThat(elements.size(), is(1));
            assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));
        }
    }

    @Test
    public void a_include_processor_class_should_be_executed_twice() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", classpath.getResource("org/asciidoctor/jruby/internal/asciidoctorclass.rb")));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(UriIncludeProcessor.class);

        for (int i = 0; i < 2; i++) {
            String content = asciidoctor.convertFile(
                    classpath.getResource("sample-with-uri-include.ad"),
                    options().toFile(false).get());

            org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

            Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

            assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));
        }
    }

    @Test
    public void a_include_processor_instance_should_be_executed_twice() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", classpath.getResource("org/asciidoctor/jruby/internal/asciidoctorclass.rb")));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(new UriIncludeProcessor(new HashMap<>()));

        for (int i = 0; i < 2; i++) {
            String content = asciidoctor.convertFile(
                    classpath.getResource("sample-with-uri-include.ad"),
                    options().toFile(false).get());

            org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

            Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

            assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));
        }
    }

    @Test
    public void a_block_processor_should_be_executed_when_registered_listing_block_is_found_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.block("yell", YellStaticListingBlock.class);
        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-yell-listing-block.ad"),
                options().toFile(false).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void a_block_processor_instance_should_be_executed_when_registered_listing_block_is_found_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        Map<String, Object> config = new HashMap<>();
        config.put(Contexts.KEY, Arrays.asList(Contexts.LISTING));
        config.put(ContentModel.KEY, ContentModel.SIMPLE);
        YellBlock yellBlock = new YellBlock("yell", config);
        javaExtensionRegistry.block(yellBlock);
        String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-yell-listing-block.ad"),
                options().toFile(false).get());
        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void should_create_toc_with_treeprocessor() {
        asciidoctor.javaExtensionRegistry().treeprocessor(new Treeprocessor() {
            @Override
            public org.asciidoctor.ast.Document process(org.asciidoctor.ast.Document document) {
                List<StructuralNode> blocks = document.getBlocks();
                for (StructuralNode block : blocks) {
                    for (StructuralNode block2 : block.getBlocks()) {
                        if (block2 instanceof Section)
                            System.out.println(((Section) block2).getId());
                    }
                }
                return document;
            }
        });

        String content = asciidoctor.convertFile(
                classpath.getResource("documentwithtoc.adoc"),
                options().headerFooter(true).toFile(false).safe(SafeMode.UNSAFE).get());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element toc = doc.getElementById("toc");
        assertThat(toc, notNullValue());
        Elements elements = toc.getElementsByAttributeValue("href", "#TestId");
        assertThat(elements.size(), is(1));
    }

    public void should_unregister_postprocessor() throws IOException {

        // Given: A registered Postprocessor
        ExtensionGroup extensionGroup = asciidoctor.createGroup(UUID.randomUUID().toString())
                .postprocessor(CustomFooterPostProcessor.class);

        // When: I render a document without registering the ExtensionGroup
        {
            Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                    .safe(SafeMode.UNSAFE).get();

            asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options);

            // Then: it is invoked
            File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
            org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");
            Element footer = doc.getElementById("footer-text");
            assertThat(footer.text(), not(containsString("Copyright Acme, Inc.")));
        }

        // When: I register the ExtensionGroup and render a document
        {
            extensionGroup.register();
            Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample.html"))
                    .safe(SafeMode.UNSAFE).get();

            asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options);

            // Then: it is invoked
            File renderedFile = new File(testFolder.getRoot(), "rendersample.html");
            org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");
            Element footer = doc.getElementById("footer-text");
            assertThat(footer.text(), containsString("Copyright Acme, Inc."));
        }
        // When: I unregister the Postprocessor and render again with the same Asciidoctor instance
        {
            extensionGroup.unregister();
            ;

            Options options2 = options().inPlace(false).toFile(new File(testFolder.getRoot(), "rendersample2.html"))
                    .safe(SafeMode.UNSAFE).get();
            asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options2);
            File renderedFile2 = new File(testFolder.getRoot(), "rendersample2.html");
            org.jsoup.nodes.Document doc2 = Jsoup.parse(renderedFile2, "UTF-8");

            Element footer2 = doc2.getElementById("footer-text");
            assertThat(footer2.text(), not(containsString("Copyright Acme, Inc.")));
        }
    }

    @Test
    public void should_unregister_block_processor() {

        Map<String, Object> config = new HashMap<>();
        config.put("contexts", Arrays.asList(":paragraph"));
        config.put("content_model", ":simple");
        YellBlock yellBlock = new YellBlock("yell", config);

        ExtensionGroup extensionGroup = this.asciidoctor.createGroup().block(yellBlock);

        {
            String contentWithoutBlock = asciidoctor.convertFile(
                    classpath.getResource("sample-with-yell-block.ad"),
                    options().toFile(false).get());
            org.jsoup.nodes.Document docWithoutBlock = Jsoup.parse(contentWithoutBlock, "UTF-8");
            Elements elementsWithoutBlock = docWithoutBlock.getElementsByClass("paragraph");
            assertThat(elementsWithoutBlock.size(), is(1));
            assertThat(elementsWithoutBlock.get(0).text(), not(is("THE TIME IS NOW. GET A MOVE ON.")));
        }

        {
            extensionGroup.register();
            String content = asciidoctor.convertFile(
                    classpath.getResource("sample-with-yell-block.ad"),
                    options().toFile(false).get());
            org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
            Elements elements = doc.getElementsByClass("paragraph");
            assertThat(elements.size(), is(1));
            assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));
        }
        {
            extensionGroup.unregister();
            String contentWithoutBlock = asciidoctor.convertFile(
                    classpath.getResource("sample-with-yell-block.ad"),
                    options().toFile(false).get());
            org.jsoup.nodes.Document docWithoutBlock = Jsoup.parse(contentWithoutBlock, "UTF-8");
            Elements elementsWithoutBlock = docWithoutBlock.getElementsByClass("paragraph");
            assertThat(elementsWithoutBlock.size(), is(1));
            assertThat(elementsWithoutBlock.get(0).text(), not(is("THE TIME IS NOW. GET A MOVE ON.")));
        }
    }

    public static class TestBlock extends BlockProcessor {

        public static Asciidoctor asciidoctor;

        @Override
        public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
            assertSame(asciidoctor, unwrap(Asciidoctor.class));
            List<String> processed = reader.readLines().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

            return createBlock(parent, "paragraph", processed, attributes, new HashMap<>());
        }

        @Override
        public String getName() {
            return "quiet";
        }
    }

    @Test
    public void a_extension_registered_as_class_can_get_its_asciidoctor_instance() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();
        TestBlock.asciidoctor = asciidoctor;
        Map<String, Object> config = new HashMap<>();
        config.put(Contexts.KEY, Arrays.asList(Contexts.LISTING));
        config.put(ContentModel.KEY, ContentModel.SIMPLE);

        javaExtensionRegistry.block("quiet", TestBlock.class);
        String content = asciidoctor.convert(
            "[quiet]\nHello World",
            options().toFile(false).get());
        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("hello world"));

        TestBlock.asciidoctor = null;
    }

    @Test
    public void a_extension_registered_as_instance_can_get_its_asciidoctor_instance() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();
        TestBlock.asciidoctor = asciidoctor;
        Map<String, Object> config = new HashMap<>();
        config.put(Contexts.KEY, Arrays.asList(Contexts.LISTING));
        config.put(ContentModel.KEY, ContentModel.SIMPLE);

        javaExtensionRegistry.block(new TestBlock());
        String content = asciidoctor.convert(
            "[quiet]\nHello Again",
            options().toFile(false).get());
        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("hello again"));

        TestBlock.asciidoctor = null;
    }


}
