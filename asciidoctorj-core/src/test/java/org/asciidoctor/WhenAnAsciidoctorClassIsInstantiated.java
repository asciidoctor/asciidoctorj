package org.asciidoctor;

import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.xpath.HasXPath.hasXPath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;
import org.asciidoctor.jruby.internal.AsciidoctorCoreException;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import com.google.common.io.CharStreams;

@RunWith(Arquillian.class)
public class WhenAnAsciidoctorClassIsInstantiated {

    @ArquillianResource
    private ClasspathResources classpath;

    @ArquillianResource
    private TemporaryFolder testFolder;

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @Test
    public void content_should_be_read_from_reader_and_written_to_writer() throws IOException, SAXException,
            ParserConfigurationException {

        FileReader inputAsciidoctorFile = new FileReader(classpath.getResource("rendersample.asciidoc"));
        StringWriter rendererWriter = new StringWriter();
        asciidoctor.convert(inputAsciidoctorFile, rendererWriter, options().asMap());

        StringBuffer renderedContent = rendererWriter.getBuffer();
        assertRenderedFile(renderedContent.toString());

    }

    @Test
    public void file_document_should_be_rendered_into_default_backend() throws IOException, SAXException,
            ParserConfigurationException {

        String render_file = asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"),
                options().toFile(false).get());
        assertRenderedFile(render_file);

    }

    @Test
    public void file_document_should_be_rendered_into_current_directory_using_options_class()
            throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {

        Options options = options().inPlace(true).get();
        File inputFile = classpath.getResource("rendersample.asciidoc");
        String renderContent = asciidoctor.convertFile(inputFile, options);

        File expectedFile = new File(inputFile.getParent(), "rendersample.html");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void file_document_should_be_rendered_into_current_directory() throws FileNotFoundException, IOException,
            SAXException, ParserConfigurationException {

        File inputFile = classpath.getResource("rendersample.asciidoc");
        String renderContent = asciidoctor.convertFile(inputFile, options()
                .inPlace(true).asMap());

        File expectedFile = new File(inputFile.getParent(), "rendersample.html");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void file_document_should_be_rendered_into_foreign_directory() throws FileNotFoundException, IOException,
            SAXException, ParserConfigurationException {

        Map<String, Object> options = options().inPlace(false).safe(SafeMode.UNSAFE).toDir(testFolder.getRoot())
                .asMap();
        String renderContent = asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options);

        File expectedFile = new File(testFolder.getRoot(), "rendersample.html");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void file_document_should_be_rendered_from_base_dir() throws IOException {

        File output = testFolder.newFolder("asciidoc", "docs");
        Options options = options().inPlace(false).baseDir(testFolder.getRoot())
                .toFile(new File("asciidoc/docs/rendersample.html")).get();
        String renderContent = asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options);

        File renderedFile = new File(output, "rendersample.html");

        assertThat(renderedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));
    }

    @Test
    public void file_document_should_be_rendered_into_foreign_directory_using_options_class()
            throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {

        Options options = options().inPlace(false).safe(SafeMode.UNSAFE).toDir(testFolder.getRoot()).get();

        String renderContent = asciidoctor.convertFile(classpath.getResource("rendersample.asciidoc"), options);

        File expectedFile = new File(testFolder.getRoot(), "rendersample.html");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));
    }

    @Test
    public void docbook_document_should_be_rendered_into_current_directory() throws FileNotFoundException, IOException,
            SAXException, ParserConfigurationException {

        Map<String, Object> attributes = attributes().backend("docbook").asMap();
        Map<String, Object> options = options().inPlace(true).attributes(attributes).asMap();

        File inputFile = classpath.getResource("rendersample.asciidoc");
        String renderContent = asciidoctor.convertFile(inputFile, options);

        File expectedFile = new File(inputFile.getParent(), "rendersample.xml");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void docbook_document_should_be_rendered_into_current_directory_using_options_class()
            throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {

        Attributes attributes = attributes().backend("docbook").get();
        Options options = options().inPlace(true).attributes(attributes).get();

        File inputFile = classpath.getResource("rendersample.asciidoc");
        String renderContent = asciidoctor.convertFile(inputFile, options);

        File expectedFile = new File(inputFile.getParent(), "rendersample.xml");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void docbook_document_should_be_rendered_into_current_directory_using_options_backend_attribute()
            throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {

        Options options = options().inPlace(true).backend("docbook").get();

        File inputFile = classpath.getResource("rendersample.asciidoc");
        String renderContent = asciidoctor.convertFile(inputFile, options);

        File expectedFile = new File(inputFile.getParent(), "rendersample.xml");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void string_content_with_custom_date_should_be_rendered() throws IOException, SAXException,
            ParserConfigurationException {

        InputStream content = new FileInputStream(classpath.getResource("documentwithdate.asciidoc"));

        Calendar customDate = Calendar.getInstance();
        customDate.set(Calendar.YEAR, 2012);
        customDate.set(Calendar.MONTH, 11);
        customDate.set(Calendar.DATE, 5);

        Map<String, Object> attributes = attributes().localDate(customDate.getTime()).asMap();
        Map<String, Object> options = options().attributes(attributes).asMap();

        String render_file = asciidoctor.convert(toString(content), options);
        assertRenderedLocalDateContent(render_file, "2012-12-05.");

    }

    @Test
    public void string_content_with_custom_time_should_be_rendered() throws IOException, SAXException,
            ParserConfigurationException {

        InputStream content = new FileInputStream(classpath.getResource("documentwithtime.asciidoc"));

        Calendar customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 15);
        customTime.set(Calendar.SECOND, 0);

        Map<String, Object> attributes = attributes().localTime(customTime.getTime()).asMap();
        Map<String, Object> options = options().attributes(attributes).asMap();

        String render_file = asciidoctor.convert(toString(content), options);

        Format TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z");

        assertRenderedLocalDateContent(render_file, TIME_FORMAT.format(customTime.getTime()) + ".");

    }

    @Test
    public void string_content_document_should_be_rendered_into_default_backend() throws IOException, SAXException,
            ParserConfigurationException {

        InputStream content = new FileInputStream(classpath.getResource("rendersample.asciidoc"));
        String render_file = asciidoctor.convert(toString(content), new HashMap<String, Object>());

        assertRenderedFile(render_file);
    }

    @Test
    public void all_files_from_a_collection_should_be_rendered_into_an_array() {

        String[] allRenderedFiles = asciidoctor.convertFiles(
                Arrays.asList(classpath.getResource("rendersample.asciidoc")), options().toFile(false).get());
        assertThat(allRenderedFiles, is(arrayWithSize(1)));

    }

    @Test
    public void all_files_from_a_collection_should_be_rendered_into_files_and_not_in_array() {

        Map<String, Object> options = options().inPlace(false).safe(SafeMode.UNSAFE).toDir(testFolder.getRoot())
                .asMap();

        String[] allRenderedFiles = asciidoctor.convertFiles(
                Arrays.asList(classpath.getResource("rendersample.asciidoc")), options);
        assertThat(allRenderedFiles, is(arrayWithSize(0)));

    }

    @Test
    public void all_files_from_directory_and_subdirectories_should_be_rendered_into_an_array() {

        File pathToWalk = classpath.getResource("src");

        String[] allRenderedFiles = asciidoctor.convertDirectory(new AsciiDocDirectoryWalker(
                        pathToWalk.getPath()),
                options().toFile(false).get());
        assertThat(allRenderedFiles, is(arrayWithSize(4)));

    }

    @Test
    public void all_files_from_directory_and_subdirectories_should_be_rendered_into_files_and_not_in_array() {

        File pathToWalk = classpath.getResource("src");
        Map<String, Object> options = options().inPlace(false).safe(SafeMode.UNSAFE).toDir(testFolder.getRoot())
                .asMap();

        String[] allRenderedFiles = asciidoctor.convertDirectory(
                new AsciiDocDirectoryWalker(pathToWalk.getAbsolutePath()),
                options);
        assertThat(allRenderedFiles, is(arrayWithSize(0)));

    }

    @Test(expected = AsciidoctorCoreException.class)
    public void an_exception_should_be_thrown_if_backend_cannot_be_resolved() {
        Options options = options().inPlace(true).backend("mybackend").get();

        File inputFile = classpath.getResource("rendersample.asciidoc");
        asciidoctor.convertFile(inputFile, options);
    }

    private void assertRenderedLocalDateContent(String render_content, String contentDateOrTime) throws IOException,
            SAXException, ParserConfigurationException {
        Source convertFileSource = new DOMSource(
                inputStream2Document(new ByteArrayInputStream(render_content.getBytes())));

        assertThat(convertFileSource, hasXPath("/div/div[@class='sectionbody']/div/p", is(contentDateOrTime)));

    }

    private void assertRenderedFile(String render_file) throws IOException, SAXException, ParserConfigurationException {
        Source renderFileSource = new DOMSource(inputStream2Document(new ByteArrayInputStream(render_file.getBytes())));

        assertThat(renderFileSource, hasXPath("/div[@class='sect1']"));
        assertThat(renderFileSource, hasXPath("/div/h2[@id='_section_a']"));
        assertThat(renderFileSource, hasXPath("/div/h2", is("Section A")));
        assertThat(renderFileSource, hasXPath("/div/div[@class='sectionbody']"));
    }

    private static String toString(InputStream inputStream) throws IOException {
        return CharStreams.toString(new InputStreamReader(inputStream));
    }

    private static org.w3c.dom.Document inputStream2Document(InputStream inputStream) throws IOException, SAXException,
            ParserConfigurationException {
        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
        newInstance.setNamespaceAware(true);
        org.w3c.dom.Document parse = newInstance.newDocumentBuilder().parse(inputStream);
        return parse;
    }

}
