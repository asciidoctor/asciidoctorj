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
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.asciidoctor.util.TestHttpServer;
import org.assertj.core.api.Assertions;
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

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenJavaExtensionGroupIsRegistered {

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
            reader.pushInclude(content.toString(), target, target, 1, attributes);
        }

        private StringBuilder readContent(String target) {
            StringBuilder content = new StringBuilder();

            try {

                URL url = new URL(target);
                URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", TestHttpServer.getInstance().getLocalPort())));
                InputStream openStream = connection.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openStream));

                String line = null;
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

        this.asciidoctor.createGroup()
                .includeProcessor(new RubyIncludeSource(new HashMap<>()))
                .register();

        String content = asciidoctor.convertFile(
                sampleWithUriInclude,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        Assertions.assertThat(contentElement.text()).contains(ASCIIDOCTORCLASS_PREFIX);

    }

    @Test
    public void an_inner_anonymous_class_should_be_registered() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

        this.asciidoctor.createGroup()
                .includeProcessor(new IncludeProcessor(new HashMap<>()) {

                    @Override
                    public void process(Document document, PreprocessorReader reader, String target,
                                        Map<String, Object> attributes) {
                        StringBuilder content = readContent(target);
                        reader.pushInclude(content.toString(), target, target, 1, attributes);
                    }

                    private StringBuilder readContent(String target) {
                        StringBuilder content = new StringBuilder();

                        try {

                            URL url = new URL(target);
                            URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", TestHttpServer.getInstance().getLocalPort())));
                            InputStream openStream = connection.getInputStream();

                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openStream));

                            String line = null;
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
                })
                .register();

        String content = asciidoctor.convertFile(
                sampleWithUriInclude,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        Assertions.assertThat(contentElement.text()).contains(ASCIIDOCTORCLASS_PREFIX);

    }

    @Test
    public void a_docinfoprocessor_should_be_executed_and_add_meta_in_header_by_default() {
        asciidoctor.createGroup()
                .docinfoProcessor(MetaRobotsDocinfoProcessor.class.getCanonicalName())
                .register();

        String content = asciidoctor.convertFile(
                simpleDocument,
                Options.builder().standalone(true).safe(SafeMode.SERVER).toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element metaRobots = doc.getElementsByAttributeValueContaining("name", "robots").first();
        assertThat(metaRobots, is(notNullValue()));
    }

    @Test
    public void a_docinfoprocessor_should_be_executed_and_add_meta_in_footer() {

        Map<String, Object> options = new HashMap<>();
        options.put("location", ":footer");
        MetaRobotsDocinfoProcessor metaRobotsDocinfoProcessor = new MetaRobotsDocinfoProcessor(options);

        this.asciidoctor.createGroup()
                .docinfoProcessor(metaRobotsDocinfoProcessor)
                .register();

        String content = asciidoctor.convertFile(
                simpleDocument,
                Options.builder().standalone(true).safe(SafeMode.SERVER).toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element footer = doc.getElementById("footer");
        // Since Asciidoctor 1.5.3 the docinfo in the footer is a sibling to the footer element
        assertEquals("robots", footer.nextElementSibling().attr("name"));
    }

    @Test
    public void a_preprocessor_should_be_executed_before_document_is_rendered() {

        this.asciidoctor.createGroup()
                .preprocessor(ChangeAttributeValuePreprocessor.class)
                .register();

        String content = asciidoctor.convertFile(
                changeAttribute,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));
    }

    @Test
    public void a_preprocessor_as_string_should_be_executed_before_document_is_rendered() {

        this.asciidoctor.createGroup()
                .preprocessor("org.asciidoctor.extension.ChangeAttributeValuePreprocessor")
                .register();

        String content = asciidoctor.convertFile(
                changeAttribute,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_preprocessor_instance_should_be_executed_before_document_is_rendered() {

        this.asciidoctor.createGroup()
                .preprocessor(new ChangeAttributeValuePreprocessor(new HashMap<>()))
                .register();

        String content = asciidoctor.convertFile(
                changeAttribute,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        assertThat(doc.getElementsByTag("p").first().text(), is("sample Alex"));

    }

    @Test
    public void a_postprocessor_as_string_should_be_executed_after_document_is_rendered() throws IOException {

        this.asciidoctor.createGroup()
                .postprocessor("org.asciidoctor.extension.CustomFooterPostProcessor")
                .register();

        File renderedFile = new File(tempFolder, "rendersample.html");
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE).
                build();

        asciidoctor.convertFile(simpleDocument, options);

        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_postprocessor_should_be_executed_after_document_is_rendered() throws IOException {

        this.asciidoctor.createGroup()
                .postprocessor(CustomFooterPostProcessor.class)
                .register();

        File renderedFile = new File(tempFolder, "rendersample.html");
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE).build();

        asciidoctor.convertFile(simpleDocument, options);

        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_postprocessor_instance_should_be_executed_after_document_is_rendered() throws IOException {

        this.asciidoctor.createGroup()
                .postprocessor(new CustomFooterPostProcessor(new HashMap<>()))
                .register();

        File renderedFile = new File(tempFolder, "rendersample.html");
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE).build();

        asciidoctor.convertFile(simpleDocument, options);

        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), containsString("Copyright Acme, Inc."));
    }

    @Test
    public void a_include_processor_as_string_should_be_executed_when_include_macro_is_found() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

        this.asciidoctor.createGroup()
                .includeProcessor("org.asciidoctor.extension.UriIncludeProcessor")
                .register();

        String content = asciidoctor.convertFile(
                sampleWithUriInclude,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        Assertions.assertThat(contentElement.text()).contains(ASCIIDOCTORCLASS_PREFIX);

    }

    @Test
    public void a_include_processor_should_be_executed_when_include_macro_is_found() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

        this.asciidoctor.createGroup()
                .includeProcessor(UriIncludeProcessor.class)
                .register();

        String content = asciidoctor.convertFile(
                sampleWithUriInclude,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        Assertions.assertThat(contentElement.text()).contains(ASCIIDOCTORCLASS_PREFIX);

    }

    @Test
    public void a_include_instance_processor_should_be_executed_when_include_macro_is_found() {

        TestHttpServer.start(Collections.singletonMap("http://example.com/asciidoctorclass.rb", asciidoctorRubyClass));

        this.asciidoctor.createGroup()
                .includeProcessor(new UriIncludeProcessor(new HashMap<>()))
                .register();

        String content = asciidoctor.convertFile(
                sampleWithUriInclude,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "language-ruby").first();

        Assertions.assertThat(contentElement.text()).contains(ASCIIDOCTORCLASS_PREFIX);

    }

    @Test
    public void a_include_processor_should_only_handle_its_handles(
            @ClasspathResource("sample-with-include.ad") File sampleWithInclude) {

        this.asciidoctor.createGroup()
                .includeProcessor(UriIncludeProcessor.class)
                .register();

        String content = asciidoctor.convertFile(sampleWithInclude,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByClass("bare").first();

        assertThat(contentElement.text(), startsWith("sample-book.adoc"));

    }

    @Test
    public void a_include_processor_can_handle_positional_attrs(
            @ClasspathResource("sample-with-include-pos-attrs.ad") File sampleWithIncludePosAttrs) {

        this.asciidoctor.createGroup()
                .includeProcessor(PositionalAttrsIncludeProcessor.class)
                .register();

        String content = asciidoctor.convertFile(sampleWithIncludePosAttrs,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "paragraph IncludeBlock").first();

        assertThat(contentElement.text(), startsWith("My,Positional,Attribute List"));

    }

    @Test
    public void a_treeprocessor_should_be_executed_in_document() {

        this.asciidoctor.createGroup()
                .treeprocessor(TerminalCommandTreeprocessor.class)
                .register();

        String content = asciidoctor.convertFile(
                sampleWithTerminalCommand,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

    }

    @Test
    public void a_treeprocessor_and_blockmacroprocessor_should_be_executed_in_document(
            @ClasspathResource("sample-with-terminal-command-and-gist-macro.ad") File sampleWithTerminalCommand) {

        this.asciidoctor.createGroup()
                .treeprocessor(TerminalCommandTreeprocessor.class)
                .blockMacro("gist", GistMacro.class)
                .register();

        String content = asciidoctor.convertFile(
                sampleWithTerminalCommand,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        System.out.println(content);

        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/42.js"));


        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));
    }

    @Test
    public void a_treeprocessor_as_string_should_be_executed_in_document() {

        this.asciidoctor.createGroup()
                .treeprocessor("org.asciidoctor.extension.TerminalCommandTreeprocessor")
                .register();

        String content = asciidoctor.convertFile(
                sampleWithTerminalCommand,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");

        Element contentElement = doc.getElementsByAttributeValue("class", "command").first();
        assertThat(contentElement.text(), is("echo \"Hello, World!\""));

        contentElement = doc.getElementsByAttributeValue("class", "command").last();
        assertThat(contentElement.text(), is("gem install asciidoctor"));

    }

    @Test
    public void a_treeprocessor_instance_should_be_executed_in_document() {

        this.asciidoctor.createGroup()
                .treeprocessor(new TerminalCommandTreeprocessor(new HashMap<>()))
                .register();

        String content = asciidoctor.convertFile(
                sampleWithTerminalCommand,
                Options.builder().toFile(false).build());

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
        this.asciidoctor.createGroup()
                .postprocessor(ArrowsAndBoxesIncludesPostProcessor.class)
                .block("arrowsAndBoxes", ArrowsAndBoxesBlock.class)
                .register();

        File renderedFile = new File(tempFolder, "rendersample.html");
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE).build();

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


        this.asciidoctor.createGroup()
                .blockMacro("gist", GistMacro.class)
                .register();

        String content = asciidoctor.convertFile(
                sampleWithGistMacro,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void a_block_macro_extension_instance_should_be_executed_when_macro_is_detected() {

        this.asciidoctor.createGroup()
                .blockMacro(new GistMacro("gist", new HashMap<>()))
                .register();

        String content = asciidoctor.convertFile(
                sampleWithGistMacro,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void a_block_macro_as_string_extension_should_be_executed_when_macro_is_detected() {

        this.asciidoctor.createGroup()
                .blockMacro("gist", "org.asciidoctor.extension.GistMacro")
                .register();

        String content = asciidoctor.convertFile(
                sampleWithGistMacro,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void a_block_macro_as_instance_extension_should_be_executed_when_macro_is_detected() {

        Map<String, Object> options = new HashMap<>();
        options.put(ContentModel.KEY, ContentModel.RAW);

        this.asciidoctor.createGroup()
                .blockMacro(new GistMacro("gist", options))
                .register();

        String content = asciidoctor.convertFile(
                sampleWithGistMacro,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element script = doc.getElementsByTag("script").first();

        assertThat(script.attr("src"), is("https://gist.github.com/123456.js"));
    }

    @Test
    public void an_inline_macro_as_string_extension_should_be_executed_when_an_inline_macro_is_detected() {

        this.asciidoctor.createGroup()
                .inlineMacro("man", "org.asciidoctor.extension.ManpageMacro")
                .register();

        String content = asciidoctor.convertFile(
                sampleWithManLink,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertThat(link.attr("href"), is("gittutorial.html"));

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void an_inline_macro_extension_should_be_executed_when_an_inline_macro_is_detected() {

        this.asciidoctor.createGroup()
                .inlineMacro("man", ManpageMacro.class)
                .register();

        String content = asciidoctor.convertFile(
                sampleWithManLink,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertThat(link.attr("href"), is("gittutorial.html"));

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void an_inline_macro_as_instance_extension_should_be_executed_when_regexp_is_set_as_option_inline_macro_is_detected() {

        Map<String, Object> options = new HashMap<>();
        options.put(InlineMacroProcessor.REGEXP, "man(?:page)?:(\\S+?)\\[(.*?)\\]");

        ManpageMacro inlineMacroProcessor = new ManpageMacro("man", options);
        this.asciidoctor.createGroup()
                .inlineMacro(inlineMacroProcessor)
                .register();

        String content = asciidoctor.convertFile(
                sampleWithManLink,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertNotNull(link);
        assertThat(link.attr("href"), is("gittutorial.html"));

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void an_inline_macro_as_instance_extension_should_not_be_executed_when_regexp_is_set_and_does_not_match() {

        Map<String, Object> options = new HashMap<>();
        options.put(InlineMacroProcessor.REGEXP, "man(?:page)?:(ThisDoesNotMatch)\\[(.*?)\\]");

        ManpageMacro inlineMacroProcessor = new ManpageMacro("man", options);
        this.asciidoctor.createGroup()
                .inlineMacro(inlineMacroProcessor)
                .register();

        String content = asciidoctor.convertFile(
                sampleWithManLink,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertNull(link);

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void an_inline_macro_as_instance_extension_should_be_executed_when_an_inline_macro_is_detected() {

        Map<String, Object> options = new HashMap<>();

        ManpageMacro inlineMacroProcessor = new ManpageMacro("man", options);
        this.asciidoctor.createGroup()
                .inlineMacro(inlineMacroProcessor)
                .register();

        String content = asciidoctor.convertFile(
                sampleWithManLink,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element link = doc.getElementsByTag("a").first();
        assertNotNull(link);
        assertThat(link.attr("href"), is("gittutorial.html"));

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        assertThat(logRecords, hasSize(0));
    }

    @Test
    public void should_unregister_all_current_registered_extensions() throws IOException {

        this.asciidoctor.createGroup()
                .postprocessor(CustomFooterPostProcessor.class)
                .register();

        File renderedFile = new File(tempFolder, "rendersample.html");
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE)
                .build();

        asciidoctor.unregisterAllExtensions();
        asciidoctor.convertFile(simpleDocument, options);

        org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element footer = doc.getElementById("footer-text");
        assertThat(footer.text(), not(containsString("Copyright Acme, Inc.")));
    }

    @Test
    public void a_block_processor_as_string_should_be_executed_when_registered_block_is_found_in_document()
            throws IOException {

        this.asciidoctor.createGroup()
                .block("yell", "org.asciidoctor.extension.YellStaticBlock")
                .register();
        String content = asciidoctor.convertFile(
                sampleWithYellBlock,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void a_block_processor_should_be_executed_when_registered_block_is_found_in_document() {

        this.asciidoctor.createGroup()
                .block("yell", YellStaticBlock.class)
                .register();
        String content = asciidoctor.convertFile(
                sampleWithYellBlock,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void a_block_processor_instance_should_be_executed_when_registered_block_is_found_in_document() {

        Map<String, Object> config = new HashMap<>();
        config.put(Contexts.KEY, Arrays.asList(Contexts.PARAGRAPH));
        config.put(ContentModel.KEY, ContentModel.SIMPLE);
        YellBlock yellBlock = new YellBlock("yell", config);
        this.asciidoctor.createGroup()
                .block(yellBlock)
                .register();
        String content = asciidoctor.convertFile(
                sampleWithYellBlock,
                Options.builder().toFile(false).build());
        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void a_block_processor_should_be_executed_when_registered_listing_block_is_found_in_document() {

        this.asciidoctor.createGroup()
                .block("yell", YellStaticListingBlock.class)
                .register();
        String content = asciidoctor.convertFile(
                sampleWithYellListingBlock,
                Options.builder().toFile(false).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void a_block_processor_instance_should_be_executed_when_registered_listing_block_is_found_in_document() {

        Map<String, Object> config = new HashMap<>();
        config.put(Contexts.KEY, Arrays.asList(Contexts.LISTING));
        config.put(ContentModel.KEY, ContentModel.SIMPLE);
        YellBlock yellBlock = new YellBlock("yell", config);
        this.asciidoctor.createGroup()
                .block(yellBlock)
                .register();
        String content = asciidoctor.convertFile(
                sampleWithYellListingBlock,
                Options.builder().toFile(false).build());
        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));

    }

    @Test
    public void should_create_toc_with_treeprocessor(
            @ClasspathResource("documentwithtoc.adoc") File documentWithToc) {

        this.asciidoctor.createGroup()
                .treeprocessor(new Treeprocessor() {
                    @Override
                    public Document process(Document document) {
                        List<StructuralNode> blocks = document.getBlocks();
                        for (StructuralNode block : blocks) {
                            for (StructuralNode block2 : block.getBlocks()) {
                                if (block2 instanceof Section)
                                    System.out.println(block2.getId());
                            }
                        }
                        return document;
                    }
                })
                .register();

        String content = asciidoctor.convertFile(
                documentWithToc,
                Options.builder().standalone(true).toFile(false).safe(SafeMode.UNSAFE).build());

        org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
        Element toc = doc.getElementById("toc");
        assertThat(toc, notNullValue());
        Elements elements = toc.getElementsByAttributeValue("href", "#TestId");
        assertThat(elements.size(), is(1));
    }

    @Test
    public void should_unregister_postprocessor() throws IOException {

        // Given: A registered Postprocessor
        ExtensionGroup extensionGroup = asciidoctor.createGroup(UUID.randomUUID().toString())
                .postprocessor(CustomFooterPostProcessor.class);

        // When: I render a document without registering the ExtensionGroup
        File renderedFile = new File(tempFolder, "rendersample.html");
        {
            Options options = Options.builder()
                    .inPlace(false)
                    .toFile(renderedFile)
                    .safe(SafeMode.UNSAFE).build();

            asciidoctor.convertFile(simpleDocument, options);

            // Then: it is invoked
            org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");
            Element footer = doc.getElementById("footer-text");
            assertThat(footer.text(), not(containsString("Copyright Acme, Inc.")));
        }

        // When: I register the ExtensionGroup and render a document
        {
            extensionGroup.register();
            Options options = Options.builder().inPlace(false).toFile(renderedFile)
                    .safe(SafeMode.UNSAFE).build();

            asciidoctor.convertFile(simpleDocument, options);

            // Then: it is invoked
            org.jsoup.nodes.Document doc = Jsoup.parse(renderedFile, "UTF-8");
            Element footer = doc.getElementById("footer-text");
            assertThat(footer.text(), containsString("Copyright Acme, Inc."));
        }
        // When: I unregister the Postprocessor and render again with the same Asciidoctor instance
        {
            extensionGroup.unregister();

            File renderedFile2 = new File(tempFolder, "rendersample2.html");
            Options options2 = Options.builder()
                    .inPlace(false)
                    .toFile(renderedFile2)
                    .safe(SafeMode.UNSAFE)
                    .build();
            asciidoctor.convertFile(simpleDocument, options2);
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
                    sampleWithYellBlock,
                    Options.builder().toFile(false).build());
            org.jsoup.nodes.Document docWithoutBlock = Jsoup.parse(contentWithoutBlock, "UTF-8");
            Elements elementsWithoutBlock = docWithoutBlock.getElementsByClass("paragraph");
            assertThat(elementsWithoutBlock.size(), is(1));
            assertThat(elementsWithoutBlock.get(0).text(), not(is("THE TIME IS NOW. GET A MOVE ON.")));
        }

        {
            extensionGroup.register();
            String content = asciidoctor.convertFile(
                    sampleWithYellBlock,
                    Options.builder().toFile(false).build());
            org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");
            Elements elements = doc.getElementsByClass("paragraph");
            assertThat(elements.size(), is(1));
            assertThat(elements.get(0).text(), is("THE TIME IS NOW. GET A MOVE ON."));
        }
        {
            extensionGroup.unregister();
            String contentWithoutBlock = asciidoctor.convertFile(
                    sampleWithYellBlock,
                    Options.builder().toFile(false).build());
            org.jsoup.nodes.Document docWithoutBlock = Jsoup.parse(contentWithoutBlock, "UTF-8");
            Elements elementsWithoutBlock = docWithoutBlock.getElementsByClass("paragraph");
            assertThat(elementsWithoutBlock.size(), is(1));
            assertThat(elementsWithoutBlock.get(0).text(), not(is("THE TIME IS NOW. GET A MOVE ON.")));
        }
    }
}
