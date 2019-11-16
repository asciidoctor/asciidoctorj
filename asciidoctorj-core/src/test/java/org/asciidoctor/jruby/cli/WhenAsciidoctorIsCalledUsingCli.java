package org.asciidoctor.jruby.cli;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WhenAsciidoctorIsCalledUsingCli {

    @ArquillianResource
    private ClasspathResources classpath;

	@ArquillianResource
	public TemporaryFolder temporaryFolder;

    public String pwd = new File("").getAbsolutePath();
	
	@Test
	public void with_no_options_file_should_be_rendered_in_place_and_in_html5_format() {
		
        File inputFile = classpath.getResource("rendersample.asciidoc");
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);
		new AsciidoctorInvoker().invoke(inputPath);
		File expectedFile = new File(inputPath.replaceFirst("\\.asciidoc$", ".html"));
		
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
	public void file_should_be_rendered_to_docbook_with_docbook_backend() {
		
        File inputFile = classpath.getResource("rendersample.asciidoc");
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);
		new AsciidoctorInvoker().invoke("-b", "docbook", inputPath);
		File expectedFile = new File(inputPath.replaceFirst("\\.asciidoc", ".xml"));
		
		assertThat(expectedFile.exists(), is(true));
		expectedFile.delete();
	}
	
	@Test
	public void single_attributes_should_be_interpreted_as_boolean() throws IOException {
		
        File inputFile = classpath.getResource("rendersample.asciidoc");
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);
		new AsciidoctorInvoker().invoke("-a", "linkcss!", inputPath);
		File expectedFile = new File(inputPath.replaceFirst("\\.asciidoc$", ".html"));
		Document doc = Jsoup.parse(expectedFile, "UTF-8");
		Elements cssStyle = doc.select("style");
		assertThat(cssStyle.html(), is(not("")));
		
		Elements link = doc.select("link");
		assertThat(link.html(), is("".trim()));
		
		expectedFile.delete();
		
	}
	
	@Test
	public void composed_attributes_should_be_built_as_attributes_map() throws IOException {
		
        File inputFile = classpath.getResource("rendersample.asciidoc");
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);
		new AsciidoctorInvoker().invoke("-a", "stylesheet=mystyles.css", "-a", "linkcss", inputPath);
		File expectedFile = new File(inputPath.replaceFirst("\\.asciidoc$", ".html"));
		
		Document doc = Jsoup.parse(expectedFile, "UTF-8");
		Elements link = doc.select("link[href]");
		String attr = link.attr("href");
		assertThat(attr, is("./mystyles.css"));
		
		expectedFile.delete();
		
	}
	
	@Test
	public void destination_dir_should_render_files_to_ouput_directory() {
		File outputDirectory = temporaryFolder.getRoot();
		
		File inputFile = classpath.getResource("rendersample.asciidoc");
		String inputPath = inputFile.getPath().substring(pwd.length() + 1);
		new AsciidoctorInvoker().invoke("-D", outputDirectory.getAbsolutePath(), inputPath);

		File expectedFile = new File(outputDirectory, inputFile.getName().replaceFirst("\\.asciidoc$", ".html"));
		assertThat(expectedFile.exists(), is(true));
		
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void empty_input_file_name_should_throw_an_exception() {
		new AsciidoctorInvoker().invoke("");
	}
	
	@Test
	public void version_flag_should_print_version_and_exit() {
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

	@Test(expected=IllegalArgumentException.class)
    public void invalid_input_file_should_throw_an_exception() {
        
        new AsciidoctorInvoker().invoke("myunknown.adoc");
        
    }
	
	@Test
	public void more_than_one_input_file_should_throw_an_exception() {
		
        File inputFile1 = classpath.getResource("rendersample.asciidoc");
        String inputPath1 = inputFile1.getPath().substring(pwd.length() + 1);
        File inputFile2 = classpath.getResource("tocsample.asciidoc");
        String inputPath2 = inputFile2.getPath().substring(pwd.length() + 1);

		new AsciidoctorInvoker().invoke(inputPath1, inputPath2);
		
		File expectedFile1 = new File(inputPath1.replaceFirst("\\.asciidoc$", ".html"));
        assertThat(expectedFile1.exists(), is(true));
        expectedFile1.delete();

		File expectedFile2 = new File(inputPath2.replaceFirst("\\.asciidoc$", ".html"));
        assertThat(expectedFile2.exists(), is(true));
        expectedFile2.delete();
        
	}
	
	@Test
	public void glob_expression_can_be_used_to_render_AsciiDoc_files() {
	    
        File inputFile = classpath.getResource("toc2sample.asciidoc");
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);
        String inputGlob = new File(new File(inputPath).getParentFile(), "toc2*.asciidoc").getPath();

	    new AsciidoctorInvoker().invoke(inputGlob);
        
		File expectedFile = new File(inputPath.replaceFirst("\\.asciidoc$", ".html"));
        
        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
	    
	}
	
	@Test
	public void help_option_should_show_usage_information() {
		ByteArrayOutputStream output = redirectStdout();
		
		new AsciidoctorInvoker().invoke("--help");
		
		String helpMessage = output.toString();
		assertThat(helpMessage, startsWith("Usage: asciidoctor [options] input file"));
		
	}
	
	@Test
    public void no_parameters_should_show_usage_information() {
        ByteArrayOutputStream output = redirectStdout();
        
        new AsciidoctorInvoker().invoke();
        
        String helpMessage = output.toString();
        assertThat(helpMessage, startsWith("Usage: asciidoctor [options] input file"));
        
    }
	
	@Test
	public void output_file_hyphen_symbol_should_render_output_to_stdout() {
		
		ByteArrayOutputStream output = redirectStdout();
		
		File inputFile = classpath.getResource("rendersample.asciidoc");
		String inputPath = inputFile.getPath().substring(pwd.length() + 1);
		new AsciidoctorInvoker().invoke("-o", "-", inputPath);
		
		Document doc = Jsoup.parse(output.toString(), "UTF-8");

		Elements link = doc.select("div[class]");
		
		String attr = link.attr("class");
		assertThat(attr, is("sect1"));
		
	}
	
	@Test
	public void verbose_option_should_fill_monitor_map() {
		
		ByteArrayOutputStream output = redirectStdout();
		
        File inputFile = classpath.getResource("rendersample.asciidoc");
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);
		new AsciidoctorInvoker().invoke("--timings", inputPath);
		
		String outputConsole = output.toString();
		assertThat(outputConsole, startsWith("  Time to read and parse source:"));
		assertThat(outputConsole, not(containsString("null")));
	}
	
	@Test
	public void quiet_option_should_not_write_to_console() {
	    
	    ByteArrayOutputStream output = redirectStdout();
	    
        File inputFile = classpath.getResource("brokeninclude.asciidoc");
        String inputPath = inputFile.getPath().substring(pwd.length() + 1);
	    new AsciidoctorInvoker().invoke("--quiet", inputPath);
	    String outputConsole = output.toString();
	    assertThat(outputConsole.trim(), is(""));
	    
	}

    @Test
    public void with_absolute_path_file_should_be_rendered() {

        File inputFile = classpath.getResource("rendersample.asciidoc");
        String inputPath = inputFile.getAbsolutePath();
        new AsciidoctorInvoker().invoke(inputPath);
        File expectedFile = new File(inputPath.replaceFirst("\\.asciidoc$", ".html"));

        assertThat(expectedFile.exists(), is(true));
        expectedFile.delete();
    }

    private ByteArrayOutputStream redirectStdout() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output));
		return output;
	}
	
}
