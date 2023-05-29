package org.asciidoctor;

import org.asciidoctor.jruby.AsciiDocDirectoryWalker;
import org.asciidoctor.jruby.internal.AsciidoctorCoreException;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xmlmatchers.xpath.HasXPath.hasXPath;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenAnAsciidoctorClassIsInstantiated {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("src")
    private File pathToWalk;

    @ClasspathResource("rendersample.asciidoc")
    private File renderSampleDocument;

    @TempDir
    private File tempFolder;


    @Test
    public void should_accept_empty_string_as_empty_content_when_output_is_String() {
        Options basicOptions = Options.builder().build();
        String result = asciidoctor.convert("", basicOptions);

        assertThat(result, isEmptyString());
    }

    @Test
    public void should_accept_null_string_as_empty_content_when_output_is_String() {
        Options basicOptions = Options.builder().build();
        String result = asciidoctor.convert(null, basicOptions);

        assertThat(result, isEmptyString());
    }

    @Test
    public void should_accept_null_string_as_empty_content_when_output_is_File() {
        File expectedFile = new File(tempFolder, "expected_empty.html");
        Options options = Options.builder()
                .safe(SafeMode.UNSAFE)
                .toFile(expectedFile)
                .build();
        String renderContent = asciidoctor.convert(null, options);

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));
    }

    @Test
    public void should_fail_when_reader_is_null() {
        Options basicOptions = Options.builder().build();
        StringWriter writer = new StringWriter();

        Throwable throwable = Assertions.catchThrowable(() -> asciidoctor.convert(null, writer, basicOptions));

        Assertions.assertThat(throwable)
                .isInstanceOf(NullPointerException.class)
                .hasMessage("reader");
    }

    @Test
    public void content_should_be_read_from_reader_and_written_to_writer()
            throws IOException, SAXException, ParserConfigurationException {

        FileReader inputAsciidoctorFile = new FileReader(renderSampleDocument);
        StringWriter rendererWriter = new StringWriter();
        asciidoctor.convert(inputAsciidoctorFile, rendererWriter, options().build().map());

        StringBuffer renderedContent = rendererWriter.getBuffer();
        assertRenderedFile(renderedContent.toString());

    }

    @Test
    public void file_document_should_be_rendered_into_default_backend() throws IOException, SAXException,
            ParserConfigurationException {

        Options options = Options.builder().toFile(false).build();
        String render_file = asciidoctor.convertFile(renderSampleDocument, options);
        assertRenderedFile(render_file);
    }

    @Test
    public void file_document_should_be_rendered_into_current_directory_using_options_class() {

        Options options = options().inPlace(true).build();
        String renderContent = asciidoctor.convertFile(renderSampleDocument, options);

        File expectedFile = new File(renderSampleDocument.getParent(), "rendersample.html");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void file_document_should_be_rendered_into_current_directory() {

        String renderContent = asciidoctor.convertFile(renderSampleDocument, options()
                .inPlace(true).build().map());

        File expectedFile = new File(renderSampleDocument.getParent(), "rendersample.html");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void file_document_should_be_rendered_into_foreign_directory() {

        final Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .build();
        String renderContent = asciidoctor.convertFile(renderSampleDocument, options);

        File expectedFile = new File(tempFolder, "rendersample.html");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void file_document_should_be_rendered_from_base_dir() {

        final File output = new File(tempFolder, "asciidoc/docs");
        output.mkdirs();
        final Options options = Options.builder()
                .inPlace(false)
                .baseDir(tempFolder)
                .toFile(new File("asciidoc/docs/rendersample.html"))
                .build();
        String renderContent = asciidoctor.convertFile(renderSampleDocument, options);

        File renderedFile = new File(output, "rendersample.html");

        assertThat(renderedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));
    }

    @Test
    public void file_document_should_be_rendered_into_foreign_directory_using_options_class() {

        Options options = options().inPlace(false).safe(SafeMode.UNSAFE).toDir(tempFolder).build();

        String renderContent = asciidoctor.convertFile(renderSampleDocument, options);

        File expectedFile = new File(tempFolder, "rendersample.html");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));
    }

    @Test
    public void docbook_document_should_be_rendered_into_current_directory() {

        Attributes attributes = Attributes.builder().backend("docbook").build();
        Map<String, Object> options = options().inPlace(true).attributes(attributes).build().map();

        String renderContent = asciidoctor.convertFile(renderSampleDocument, options);

        File expectedFile = new File(renderSampleDocument.getParent(), "rendersample.xml");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void docbook_document_should_be_rendered_into_current_directory_using_options_class() {

        Attributes attributes = Attributes.builder().backend("docbook").build();
        Options options = options().inPlace(true).attributes(attributes).build();

        String renderContent = asciidoctor.convertFile(renderSampleDocument, options);

        File expectedFile = new File(renderSampleDocument.getParent(), "rendersample.xml");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void docbook_document_should_be_rendered_into_current_directory_using_options_backend_attribute() {

        Options options = options().inPlace(true).backend("docbook").build();

        String renderContent = asciidoctor.convertFile(renderSampleDocument, options);

        File expectedFile = new File(renderSampleDocument.getParent(), "rendersample.xml");

        assertThat(expectedFile.exists(), is(true));
        assertThat(renderContent, is(nullValue()));

        expectedFile.delete();
    }

    @Test
    public void string_content_with_custom_date_should_be_rendered(
            @ClasspathResource("documentwithdate.asciidoc") Path documentWithDate)
            throws IOException, SAXException, ParserConfigurationException {

        Calendar customDate = Calendar.getInstance();
        customDate.set(Calendar.YEAR, 2012);
        customDate.set(Calendar.MONTH, 11);
        customDate.set(Calendar.DATE, 5);

        Attributes attributes = Attributes.builder().localDate(customDate.getTime()).build();
        Map<String, Object> options = options().attributes(attributes).build().map();

        String render_file = asciidoctor.convert(Files.readString(documentWithDate), options);
        assertRenderedLocalDateContent(render_file, "2012-12-05.");

    }

    @Test
    public void string_content_with_custom_time_should_be_rendered(
            @ClasspathResource("documentwithtime.asciidoc") Path documentWithDate) throws IOException, SAXException,
            ParserConfigurationException {

        Calendar customTime = Calendar.getInstance();
        customTime.set(Calendar.HOUR_OF_DAY, 23);
        customTime.set(Calendar.MINUTE, 15);
        customTime.set(Calendar.SECOND, 0);

        Attributes attributes = Attributes.builder().localTime(customTime.getTime()).build();
        Map<String, Object> options = options().attributes(attributes).build().map();

        String render_file = asciidoctor.convert(Files.readString(documentWithDate), options);

        Format TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z");

        assertRenderedLocalDateContent(render_file, TIME_FORMAT.format(customTime.getTime()) + ".");

    }

    @Test
    public void string_content_document_should_be_rendered_into_default_backend() throws IOException, SAXException,
            ParserConfigurationException {

        String render_file = asciidoctor.convert(Files.readString(renderSampleDocument.toPath()), new HashMap<>());

        assertRenderedFile(render_file);
    }

    @Test
    public void all_files_from_a_collection_should_be_rendered_into_an_array() {

        String[] allRenderedFiles = asciidoctor.convertFiles(
                List.of(renderSampleDocument), options().toFile(false).build());
        assertThat(allRenderedFiles, is(arrayWithSize(1)));
    }

    @Test
    public void all_files_from_a_collection_should_be_rendered_into_files_and_not_in_array() {

        Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .build();

        String[] allRenderedFiles = asciidoctor.convertFiles(
                Arrays.asList(renderSampleDocument), options);
        assertThat(allRenderedFiles, is(arrayWithSize(0)));

    }

    @Test
    public void all_files_from_directory_and_subdirectories_should_be_rendered_into_an_array() {

        String[] allRenderedFiles = asciidoctor.convertDirectory(
                new AsciiDocDirectoryWalker(pathToWalk.getPath()),
                Options.builder().toFile(false).build());
        assertThat(allRenderedFiles, is(arrayWithSize(4)));

    }

    @Test
    public void all_files_from_directory_and_subdirectories_should_be_rendered_into_files_and_not_in_array() {

        final Options options = Options.builder()
                .inPlace(false)
                .safe(SafeMode.UNSAFE)
                .toDir(tempFolder)
                .build();
        String[] allRenderedFiles = asciidoctor.convertDirectory(
                new AsciiDocDirectoryWalker(pathToWalk.getAbsolutePath()),
                options);
        assertThat(allRenderedFiles, is(arrayWithSize(0)));

    }

    @Test
    public void an_exception_should_be_thrown_if_backend_cannot_be_resolved() {
        assertThrows(AsciidoctorCoreException.class, () -> {
            final Options options = Options.builder()
                    .inPlace(true)
                    .backend("mybackend")
                    .build();
            asciidoctor.convertFile(renderSampleDocument, options);
        });
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

    private static org.w3c.dom.Document inputStream2Document(InputStream inputStream) throws IOException, SAXException,
            ParserConfigurationException {
        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
        newInstance.setNamespaceAware(true);
        org.w3c.dom.Document parse = newInstance.newDocumentBuilder().parse(inputStream);
        return parse;
    }

    private OptionsBuilder options() {
        return Options.builder();
    }
}
