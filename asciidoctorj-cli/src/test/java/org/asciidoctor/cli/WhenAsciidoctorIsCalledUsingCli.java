package org.asciidoctor.cli;

import org.asciidoctor.cli.jruby.AsciidoctorInvoker;
import org.asciidoctor.util.ClasspathHelper;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

public class WhenAsciidoctorIsCalledUsingCli {

    private static final String SAMPLE_FILE = "rendersample.asciidoc";
    private static final String BROKEN_INCLUDE_ASCIIDOC = "brokeninclude.asciidoc";
    private static final String TOC_SAMPLE_ASCIIDOC = "tocsample.asciidoc";

    private static final String SOURCE_EXTENSION_PATTERN = "\\.asciidoc$";

    private static ClasspathHelper classpath;

    @TempDir
    public File temporaryFolder;

    public String pwd = new File("").getAbsolutePath();

    @BeforeAll
    static void beforeAll() {
        classpath = new ClasspathHelper();
        classpath.setClassloader(WhenAsciidoctorIsCalledUsingCli.class);
    }

    @Test
    public void with_no_options_file_should_be_rendered_in_place_and_in_html5_format() throws IOException {
        File inputFile = classpath.getResource(SAMPLE_FILE);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke(inputPath);

        File expectedFile = new File(inputPath.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
    }

    @Test
    public void should_honor_doctype_defined_in_document_by_default() throws IOException {
        File inputFile = classpath.getResource("sample-book.adoc");
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);

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
    public void file_should_be_rendered_to_docbook_with_docbook_backend() throws IOException {
        File inputFile = classpath.getResource(SAMPLE_FILE);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("-b", "docbook", inputPath);

        File expectedFile = new File(inputPath.replaceFirst("\\.asciidoc", ".xml"));
        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
    }

    @Test
    public void single_attributes_should_be_interpreted_as_boolean() throws IOException {
        File inputFile = classpath.getResource(SAMPLE_FILE);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);

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
    public void composed_attributes_should_be_built_as_attributes_map() throws IOException {
        File inputFile = classpath.getResource(SAMPLE_FILE);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);

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
    public void destination_dir_should_render_files_to_ouput_directory() throws IOException {
        File outputDirectory = temporaryFolder;

        File inputFile = classpath.getResource(SAMPLE_FILE);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);
        new AsciidoctorInvoker().invoke("-D", outputDirectory.getAbsolutePath(), inputPath);

        File expectedFile = new File(outputDirectory, inputFile.getName().replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile.exists(), is(true));
    }


    @Test
    public void empty_input_file_name_should_throw_an_exception() {
        Throwable throwable = catchThrowable(() -> new AsciidoctorInvoker().invoke(""));

        Assertions.assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void version_flag_should_print_version_and_exit() throws IOException {
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
    public void invalid_input_file_should_throw_an_exception() {
        Throwable throwable = catchThrowable(() -> new AsciidoctorInvoker().invoke("myunknown.adoc"));

        Assertions.assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_convert_multiple_inputs() throws IOException {
        File inputFile1 = classpath.getResource(SAMPLE_FILE);
        String inputPath1 = inputFile1.getPath().substring(pwd.length() + 1);
        File inputFile2 = classpath.getResource(TOC_SAMPLE_ASCIIDOC);
        String inputPath2 = inputFile2.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke(inputPath1, inputPath2);

        File expectedFile1 = new File(inputPath1.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile1.exists(), is(true));
        expectedFile1.delete();

        File expectedFile2 = new File(inputPath2.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile2.exists(), is(true));
        expectedFile2.delete();
    }

    @Test
    public void glob_expression_can_be_used_to_render_AsciiDoc_files() throws IOException {
        File inputFile = classpath.getResource(TOC_SAMPLE_ASCIIDOC);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);
        String inputGlob = new File(new File(inputPath).getParentFile(), "toc*ple.asciidoc").getPath();

        new AsciidoctorInvoker().invoke(inputGlob);

        File expectedFile = new File(inputPath.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));

        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
    }

    @Test
    public void help_option_should_show_usage_information() throws IOException {
        ByteArrayOutputStream output = redirectStdout();

        new AsciidoctorInvoker().invoke("--help");

        String helpMessage = output.toString();
        assertThat(helpMessage, startsWith("Usage: asciidoctor [options] input file"));
    }

    @Test
    public void no_parameters_should_show_usage_information() throws IOException {
        ByteArrayOutputStream output = redirectStdout();

        new AsciidoctorInvoker().invoke();

        String helpMessage = output.toString();
        assertThat(helpMessage, startsWith("Usage: asciidoctor [options] input file"));
    }

    @Test
    public void output_file_hyphen_symbol_should_render_output_to_stdout() throws IOException {
        ByteArrayOutputStream output = redirectStdout();
        File inputFile = classpath.getResource(SAMPLE_FILE);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("-o", "-", inputPath);

        Document doc = Jsoup.parse(output.toString(), "UTF-8");
        Elements link = doc.select("div[class]");
        String attr = link.attr("class");
        assertThat(attr, is("sect1"));
    }

    @Test
    public void verbose_option_should_fill_monitor_map() throws IOException {
        ByteArrayOutputStream output = redirectStdout();
        File inputFile = classpath.getResource(SAMPLE_FILE);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("--timings", inputPath);

        String outputConsole = output.toString();
        assertThat(outputConsole, startsWith("  Time to read and parse source:"));
        assertThat(outputConsole, not(containsString("null")));
    }

    @Test
    public void quiet_option_should_not_write_to_console() throws IOException {
        ByteArrayOutputStream output = redirectStdout();
        File inputFile = classpath.getResource(BROKEN_INCLUDE_ASCIIDOC);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);

        new AsciidoctorInvoker().invoke("--quiet", inputPath);

        String outputConsole = output.toString();
        assertThat(outputConsole.trim(), is(""));
    }

    @Test
    public void should_exit_with_zero_status_even_if_errors_were_logged() throws IOException {
        File inputFile = classpath.getResource(BROKEN_INCLUDE_ASCIIDOC);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);

        int status = new AsciidoctorInvoker().invoke(inputPath);

        assertThat(status, is(0));
    }

    @Test
    public void should_exit_with_nonzero_status_if_logged_severity_was_at_least_failure_level() throws IOException {
        File inputFile = classpath.getResource(BROKEN_INCLUDE_ASCIIDOC);
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);

        int status = new AsciidoctorInvoker().invoke("--failure-level", "warn", inputPath);

        assertThat(status, is(1));
    }

    @Test
    public void with_absolute_path_file_should_be_rendered() throws IOException {
        File inputFile = classpath.getResource(SAMPLE_FILE);
        String inputPath = inputFile.getAbsolutePath();

        new AsciidoctorInvoker().invoke(inputPath);

        File expectedFile = new File(inputPath.replaceFirst(SOURCE_EXTENSION_PATTERN, ".html"));
        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
    }

    @Test
    public void should_convert_to_subdirectories() throws IOException {
        File inputFile = classpath.getResource("relative/sub/test.adoc");
        File srcDir = inputFile.getParentFile().getParentFile();
        File toDir = new File(srcDir, "target");

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
