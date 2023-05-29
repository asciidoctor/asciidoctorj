package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.TestLogHandlerService;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.asciidoctor.util.TestHttpServer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenJavaExtensionIsRegistered {

    public static final String ASCIIDOCTORCLASS_PREFIX = "module AsciidoctorJ    include_package 'org.asciidoctor'";

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @ClasspathResource("org/asciidoctor/jruby/internal/asciidoctorclass.rb")
    private File asciidoctorRubyClass;

    @ClasspathResource("rendersample.asciidoc")
    private File renderSample;

    @ClasspathResource("simple.adoc")
    private File simpleDocument;

    @ClasspathResource("changeattribute.adoc")
    private File changeAttribute;

    @ClasspathResource("sample-with-terminal-command.ad")
    private File sampleWithTerminalCommand;

    @ClasspathResource("sample-with-uri-include.ad")
    private File sampleWithUriInclude;

    @ClasspathResource("sample-with-yell-block.ad")
    private File sampleWithYellBlock;

    @ClasspathResource("sample-with-gist-macro.ad")
    private File sampleWithGistMacro;

    @ClasspathResource("sample-with-man-link.ad")
    private File sampleWithManLink;

    @ClasspathResource("sample-with-yell-listing-block.ad")
    private File sampleWithYellListingBlock;

    @TempDir
    public File tempFolder;


    @BeforeEach
    public void before() {
        TestLogHandlerService.clear();
    }

    @AfterEach
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

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(new RubyIncludeSource(new HashMap<>()));

        String content = asciidoctor.convertFile(
                sampleWithUriInclude,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));

    }

    @Test
    public void an_inner_anonymous_class_should_be_registered() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

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
                sampleWithUriInclude,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));

    }

    @Test
    public void a_docinfoprocessor_should_be_executed_and_add_meta_in_header_by_default() {
        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.docinfoProcessor(MetaRobotsDocinfoProcessor.class.getCanonicalName());

        String content = asciidoctor.convertFile(
                simpleDocument,
                options().standalone(true).safe(SafeMode.SERVER).toFile(false).build());

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
                simpleDocument,
                options().standalone(true).safe(SafeMode.SERVER).toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element footer = doc.getElementById("footer");
        // Since Asciidoctor 1.5.3 the docinfo in the footer is a sibling to the footer element
        assertEquals("robots", footer.nextElementSibling().attr("name"));
    }

    @Test
    public void a_preprocessor_should_be_executed_before_document_is_rendered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(ChangeAttributeValuePreprocessor.class);

        String content = asciidoctor.convertFile(
                changeAttribute,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_preprocessor_as_string_should_be_executed_before_document_is_rendered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor("org.asciidoctor.extension.ChangeAttributeValuePreprocessor");

        String content = asciidoctor.convertFile(
                changeAttribute,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_preprocessor_instance_should_be_executed_before_document_is_rendered() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.preprocessor(new ChangeAttributeValuePreprocessor(new HashMap<>()));

        String content = asciidoctor.convertFile(
                changeAttribute,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_postprocessor_as_string_should_be_executed_after_document_is_rendered() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor("org.asciidoctor.extension.CustomFooterPostProcessor");

        File renderedFile = new File(tempFolder, "rendersample.html");
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE)
                .build();

        asciidoctor.convertFile(renderSample, options);

        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_postprocessor_should_be_executed_after_document_is_rendered() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor(CustomFooterPostProcessor.class);

        File renderedFile = new File(tempFolder, "rendersample.html");
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE)
                .build();

        asciidoctor.convertFile(renderSample, options);

        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_postprocessor_instance_should_be_executed_after_document_is_rendered() throws IOException {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.postprocessor(new CustomFooterPostProcessor(new HashMap<>()));

        File renderedFile = new File(tempFolder, "rendersample.html");
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE).build();

        asciidoctor.convertFile(renderSample, options);

        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_include_processor_as_string_should_be_executed_when_include_macro_is_found() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor("org.asciidoctor.extension.UriIncludeProcessor");

        String content = asciidoctor.convertFile(
                sampleWithUriInclude,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));

    }

    @Test
    public void a_include_processor_should_be_executed_when_include_macro_is_found() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(UriIncludeProcessor.class);

        String content = asciidoctor.convertFile(
                sampleWithUriInclude,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));

    }

    @Test
    public void a_include_instance_processor_should_be_executed_when_include_macro_is_found() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(new UriIncludeProcessor(new HashMap<>()));

        String content = asciidoctor.convertFile(
                sampleWithUriInclude,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));

    }

    @Test
    public void a_include_processor_should_only_handle_its_handles(
            @ClasspathResource("sample-with-include.ad") File sampleWithInclude) {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(UriIncludeProcessor.class);

        String content = asciidoctor.convertFile(sampleWithInclude,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByClass("bare").first();

        assertThat(contentElement.text(), startsWith("sample-book.adoc"));

    }

    @Test
    public void a_include_processor_can_handle_positional_attrs(
            @ClasspathResource("sample-with-include-pos-attrs.ad") File sampleWithIncludePosAttrs) {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(PositionalAttrsIncludeProcessor.class);

        String content = asciidoctor.convertFile(sampleWithIncludePosAttrs,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "paragraph IncludeBlock").first();

        assertThat(contentElement.text(), startsWith("My,Positional,Attribute List"));

    }

    @Test
    public void a_treeprocessor_should_be_executed_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.treeprocessor(TerminalCommandTreeprocessor.class);

        String content = asciidoctor.convertFile(
                sampleWithTerminalCommand,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

    }

    @Test
    public void a_treeprocessor_and_blockmacroprocessor_should_be_executed_in_document(
            @ClasspathResource("sample-with-terminal-command-and-gist-macro.ad") File sampleWithTerminalCommand) {

        this.asciidoctor.javaExtensionRegistry()
                .treeprocessor(TerminalCommandTreeprocessor.class)
                .blockMacro("gist", GistMacro.class);

        String content = asciidoctor.convertFile(
                sampleWithTerminalCommand,
                options().toFile(false).build());

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
                sampleWithTerminalCommand,
                options().toFile(false).build());

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
                sampleWithTerminalCommand,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

    }

    @Test
    @Disabled
    public void extensions_should_be_correctly_added_using_extension_registry(
            @ClasspathResource("arrows-and-boxes-example.ad") File arrowsAndBoxesExample) throws IOException {

        // To avoid registering the same extension over and over for all tests,
        // service is instantiated manually.
        new ArrowsAndBoxesExtension().register(asciidoctor);

        File renderedFile = new File(tempFolder, "rendersample.html");
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE)
                .build();

        asciidoctor.convertFile(arrowsAndBoxesExample, options);

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
                sampleWithGistMacro,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void a_block_macro_extension_instance_should_be_executed_when_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.blockMacro(new GistMacro("gist", new HashMap<>()));

        String content = asciidoctor.convertFile(
                sampleWithGistMacro,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void a_block_macro_as_string_extension_should_be_executed_when_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.blockMacro("gist", "org.asciidoctor.extension.GistMacro");

        String content = asciidoctor.convertFile(
                sampleWithGistMacro,
                options().toFile(false).build());

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
                sampleWithGistMacro,
                options().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void an_inline_macro_as_string_extension_should_be_executed_when_an_inline_macro_is_detected() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.inlineMacro("man", "org.asciidoctor.extension.ManpageMacro");

        String content = asciidoctor.convertFile(
                sampleWithManLink,
                options().toFile(false).build());

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
                sampleWithManLink,
                options().toFile(false).build());

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
                sampleWithManLink,
                options().toFile(false).build());

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
                sampleWithManLink,
                options().toFile(false).build());

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
                sampleWithManLink,
                options().toFile(false).build());

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

        File renderedFile = new File(tempFolder, "rendersample.html");
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE).build();

        asciidoctor.unregisterAllExtensions();
        asciidoctor.convertFile(renderSample, options);

        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), not(containsString("Copyright Acme, Inc.")));
    }

    @Test
    public void a_block_processor_as_string_should_be_executed_when_registered_block_is_found_in_document() {

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.block("yell", "org.asciidoctor.extension.YellStaticBlock");
        String content = asciidoctor.convertFile(
                sampleWithYellBlock,
                options().toFile(false).build());

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
                sampleWithYellBlock,
                options().toFile(false).build());

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
                    sampleWithYellBlock,
                    options().toFile(false).build());

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
                sampleWithYellBlock,
                options().toFile(false).build());
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
                    sampleWithYellBlock,
                    options().toFile(false).build());
            org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
            Elements elements = doc.getElementsByClass("paragraph");
            assertThat(elements.size(), is(1));
            assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));
        }
    }

    @Test
    public void a_include_processor_class_should_be_executed_twice() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(UriIncludeProcessor.class);

        for (int i = 0; i < 2; i++) {
            String content = asciidoctor.convertFile(
                    sampleWithUriInclude,
                    options().toFile(false).build());

            org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

            Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

            assertThat(contentElement.text(), startsWith(ASCIIDOCTORCLASS_PREFIX));
        }
    }

    @Test
    public void a_include_processor_instance_should_be_executed_twice() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

        JavaExtensionRegistry javaExtensionRegistry = this.asciidoctor.javaExtensionRegistry();

        javaExtensionRegistry.includeProcessor(new UriIncludeProcessor(new HashMap<>()));

        for (int i = 0; i < 2; i++) {
            String content = asciidoctor.convertFile(
                    sampleWithUriInclude,
                    options().toFile(false).build());

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
                sampleWithYellListingBlock,
                options().toFile(false).build());

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
                sampleWithYellListingBlock,
                options().toFile(false).build());
        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void should_create_toc_with_treeprocessor(
            @ClasspathResource("documentwithtoc.adoc") File documentWithToc) {

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
                documentWithToc,
                options().standalone(true).toFile(false).safe(SafeMode.UNSAFE).build());

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
            File renderedFile = new File(tempFolder, "rendersample.html");
            Options options = Options.builder()
                    .inPlace(false)
                    .toFile(renderedFile)
                    .safe(SafeMode.UNSAFE)
                    .build();

            asciidoctor.convertFile(renderSample, options);

            // Then: it is invoked
            org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");
            Element footer = doc.getElementById("footer-text");
            assertThat(footer.text(), not(containsString("Copyright Acme, Inc.")));
        }

        // When: I register the ExtensionGroup and render a document
        {
            extensionGroup.register();
            File renderedFile = new File(tempFolder, "rendersample.html");
            Options options = Options.builder()
                    .inPlace(false)
                    .toFile(renderedFile)
                    .safe(SafeMode.UNSAFE)
                    .build();

            asciidoctor.convertFile(renderSample, options);

            // Then: it is invoked
            org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");
            Element footer = doc.getElementById("footer-text");
            assertThat(footer.text(), containsString("Copyright Acme, Inc."));
        }
        // When: I unregister the Postprocessor and render again with the same Asciidoctor instance
        {
            extensionGroup.unregister();
            ;

            File renderedFile2 = new File(tempFolder, "rendersample2.html");
            Options options2 = Options.builder()
                    .inPlace(false)
                    .toFile(renderedFile2)
                    .safe(SafeMode.UNSAFE)
                    .build();
            asciidoctor.convertFile(renderSample, options2);
            org.jsoup.nodes.Document doc2 = Jsoup.parse(renderedFile2, "UTF-8");

            Element footer2 = doc2.getElementById("footer-text");
            assertThat(footer2.text(), not(containsString("Copyright Acme, Inc.")));
        }
    }

    @Test
    public void should_unregister_block_processor() {

        Map<String, Object> config = new HashMap<>();
        config.put("contexts", Collections.singletonList(":paragraph"));
        config.put("content_model", ":simple");
        YellBlock yellBlock = new YellBlock("yell", config);

        ExtensionGroup extensionGroup = this.asciidoctor.createGroup().block(yellBlock);

        {
            String contentWithoutBlock = asciidoctor.convertFile(
                    sampleWithYellBlock,
                    options().toFile(false).build());
            org.jsoup.nodes.Document docWithoutBlock = Jsoup.parse(contentWithoutBlock, "UTF-8");
            Elements elementsWithoutBlock = docWithoutBlock.getElementsByClass("paragraph");
            assertThat(elementsWithoutBlock.size(), is(1));
            assertThat(elementsWithoutBlock.get(0).text(), not(is("THE TIME IS NOW. GET A MOVE ON.")));
        }

        {
            extensionGroup.register();
            String content = asciidoctor.convertFile(
                    sampleWithYellBlock,
                    options().toFile(false).build());
            org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
            Elements elements = doc.getElementsByClass("paragraph");
            assertThat(elements.size(), is(1));
            assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));
        }
        {
            extensionGroup.unregister();
            String contentWithoutBlock = asciidoctor.convertFile(
                    sampleWithYellBlock,
                    options().toFile(false).build());
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
                options().toFile(false).build());
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
                options().toFile(false).build());
        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("hello again"));

        TestBlock.asciidoctor = null;
    }

    private OptionsBuilder options() {
        return Options.builder();
    }

}
