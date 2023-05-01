package org.asciidoctor.cli;

import org.asciidoctor.cli.jruby.AsciidoctorInvoker;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;

@ExtendWith(ClasspathExtension.class)
public class WhenAsciidoctorIsCalledUsingCli {

    static final String SOURCE_EXTENSION_PATTERN = "\\.asciidoc$";

    @ClasspathResource("rendersample.asciidoc")
    private File renderSampleDocument;
    @ClasspathResource("brokeninclude.asciidoc")
    private File brokenIncludeDocument;
    @ClasspathResource("tocsample.asciidoc")
    private File tocSampleDocument;

    final String pwd = new File("").getAbsolutePath();


    @Test
    void with_no_options_file_should_be_rendered_in_place_and_in_html5_format() throws IOException {
        final String inputPath = renderSampleDocument.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke(inputPath);

        File expectedFile = new File(inputPath.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
    }

    @Test
    void should_honor_doctype_defined_in_document_by_default(@ClasspathResource("sample-book.adoc") File sampleBookDocument) throws IOException {
        final String inputPath = sampleBookDocument.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke(inputPath);

        File expectedFile = new File(inputPath.replaceFirst("\\.adoc$", ".html"));
        assertThat(expectedFile.exists(), is(true));
        Document doc = Jsoup.parse(expectedFile, "UTF-8");
        Elements body = doc.select("body");
        String attr = body.attr("class");
        assertThat(attr, is("book"));
        expectedFile.delete();
    }

    @Test
    void file_should_be_rendered_to_docbook_with_docbook_backend() throws IOException {
        final String inputPath = renderSampleDocument.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("-b", "docbook", inputPath);

        File expectedFile = new File(inputPath.replaceFirst("\\.asciidoc", ".xml"));
        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
    }

    @Test
    void single_attributes_should_be_interpreted_as_boolean() throws IOException {
        final String inputPath = renderSampleDocument.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("-a", "linkcss!", inputPath);

        File expectedFile = new File(inputPath.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        Document doc = Jsoup.parse(expectedFile, "UTF-8");
        Elements cssStyle = doc.select("style");
        assertThat(cssStyle.html(), is(not("")));

        Elements link = doc.select("link");
        assertThat(link.html(), is("".trim()));

        expectedFile.delete();
    }

    @Test
    void composed_attributes_should_be_built_as_attributes_map() throws IOException {
        final String inputPath = renderSampleDocument.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("-a", "stylesheet=mystyles.css", "-a", "linkcss", inputPath);

        File expectedFile = new File(inputPath.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile.exists(), is(true));
        String attr = Jsoup.parse(expectedFile, "UTF-8")
                .select("link[href]")
                .attr("href");
        assertThat(attr, is("./mystyles.css"));

        expectedFile.delete();
    }

    @Test
    void destination_dir_should_render_files_to_ouput_directory(@TempDir File temporaryFolder) throws IOException {
        final String inputPath = renderSampleDocument.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("-D", temporaryFolder.getAbsolutePath(), inputPath);

        File expectedFile = new File(temporaryFolder, renderSampleDocument.getName().replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile.exists(), is(true));
    }


    @Test
    void empty_input_file_name_should_throw_an_exception() {
        Throwable throwable = catchThrowable(() -> new AsciidoctorInvoker().invoke(""));

        Assertions.assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void version_flag_should_print_version_and_exit() throws IOException {
        PrintStream oldOs = System.out;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        System.setOut(new PrintStream(os));
        try {
            new AsciidoctorInvoker().invoke("-V");
        } finally {
            System.setOut(oldOs);
        }
        assertThat(os.toString(), startsWith("Asciidoctor"));
    }

    @Test
    void invalid_input_file_should_throw_an_exception() {
        Throwable throwable = catchThrowable(() -> new AsciidoctorInvoker().invoke("myunknown.adoc"));

        Assertions.assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_convert_multiple_inputs() throws IOException {
        final String inputPath1 = renderSampleDocument.getPath().substring(pwd.length() + 1);
        final String inputPath2 = tocSampleDocument.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke(inputPath1, inputPath2);

        File expectedFile1 = new File(inputPath1.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile1.exists(), is(true));
        expectedFile1.delete();

        File expectedFile2 = new File(inputPath2.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile2.exists(), is(true));
        expectedFile2.delete();
    }

    @Test
    void glob_expression_can_be_used_to_render_AsciiDoc_files() throws IOException {
        final String inputPath = tocSampleDocument.getPath().substring(pwd.length() + 1);
        final String inputGlob = new File(new File(inputPath).getParentFile(), "toc*ple.asciidoc").getPath();

        new AsciidoctorInvoker().invoke(inputGlob);

        File expectedFile = new File(inputPath.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));

        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
    }

    @Test
    void help_option_should_show_usage_information() throws IOException {
        final ByteArrayOutputStream output = redirectStdout();

        new AsciidoctorInvoker().invoke("--help");

        String helpMessage = output.toString();
        assertThat(helpMessage, startsWith("Usage: asciidoctor [options] input file"));
    }

    @Test
    void no_parameters_should_show_usage_information() throws IOException {
        final ByteArrayOutputStream output = redirectStdout();

        new AsciidoctorInvoker().invoke();

        String helpMessage = output.toString();
        assertThat(helpMessage, startsWith("Usage: asciidoctor [options] input file"));
    }

    @Test
    void output_file_hyphen_symbol_should_render_output_to_stdout() throws IOException {
        final ByteArrayOutputStream output = redirectStdout();
        final String inputPath = renderSampleDocument.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("-o", "-", inputPath);

        Document doc = Jsoup.parse(output.toString(), "UTF-8");
        Elements link = doc.select("div[class]");
        String attr = link.attr("class");
        assertThat(attr, is("sect1"));
    }

    @Test
    void verbose_option_should_fill_monitor_map() throws IOException {
        final ByteArrayOutputStream output = redirectStdout();
        final String inputPath = renderSampleDocument.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("--timings", inputPath);

        String outputConsole = output.toString();
        assertThat(outputConsole, startsWith("  Time to read and parse source:"));
        assertThat(outputConsole, not(containsString("null")));
    }

    @Test
    void quiet_option_should_not_write_to_console() throws IOException {
        final ByteArrayOutputStream output = redirectStdout();
        final String inputPath = brokenIncludeDocument.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("--quiet", inputPath);

        String outputConsole = output.toString();
        assertThat(outputConsole.trim(), is(""));
    }

    @Test
    void should_exit_with_zero_status_even_if_errors_were_logged() throws IOException {
        final String inputPath = brokenIncludeDocument.getPath().substring(pwd.length() + 1);

        int status = new AsciidoctorInvoker().invoke(inputPath);

        assertThat(status, is(0));
    }

    @Test
    void should_exit_with_nonzero_status_if_logged_severity_was_at_least_failure_level() throws IOException {
        String inputPath = brokenIncludeDocument.getPath().substring(pwd.length() + 1);

        int status = new AsciidoctorInvoker().invoke("--failure-level", "warn", inputPath);

        assertThat(status, is(1));
    }

    @Test
    void with_absolute_path_file_should_be_rendered() throws IOException {
        String inputPath = renderSampleDocument.getAbsolutePath();

        new AsciidoctorInvoker().invoke(inputPath);

        File expectedFile = new File(inputPath.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
    }

    @Test
    void should_convert_to_subdirectories(@ClasspathResource("relative/sub/test.adoc") File relativePathDocument) throws IOException {
        final File srcDir = relativePathDocument.getParentFile().getParentFile();
        final File toDir = new File(srcDir, "target");

        new AsciidoctorInvoker().invoke("-R", srcDir.getPath(), "-D", toDir.getPath(), srcDir.getAbsolutePath() + "/**/*.adoc");

        File expectedFile = new File(toDir, "sub/test.html");
        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
    }

    private ByteArrayOutputStream redirectStdout() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        return output;
    }
}
