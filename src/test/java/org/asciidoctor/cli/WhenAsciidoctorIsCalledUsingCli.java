package org.asciidoctor.cli;

import static org.hamcrest.core.StringStartsWith.startsWith; 
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WhenAsciidoctorIsCalledUsingCli {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@Test
	public void with_no_options_file_should_be_rendered_in_place_and_in_html5_format() {
		
		new AsciidoctorInvoker().invoke("target/test-classes/rendersample.asciidoc");
		File expectedFile = new File("target/test-classes/rendersample.html");
		
		assertThat(expectedFile.exists(), is(true));
		expectedFile.delete();
	}
	
	@Test
	public void file_should_be_rendered_to_docbook_with_docbook_backend() {
		
		new AsciidoctorInvoker().invoke("-b", "docbook", "target/test-classes/rendersample.asciidoc");
		File expectedFile = new File("target/test-classes/rendersample.xml");
		
		assertThat(expectedFile.exists(), is(true));
		expectedFile.delete();
	}
	
	@Test
	public void single_attributes_should_be_interpreted_as_boolean() throws IOException {
		
		new AsciidoctorInvoker().invoke("-a", "linkcss!", "target/test-classes/rendersample.asciidoc");
		File expectedFile = new File("target/test-classes/rendersample.html");
		Document doc = Jsoup.parse(expectedFile, "UTF-8");
		Elements cssStyle = doc.select("style");
		assertThat(cssStyle.html(), is(not("")));
		
		Elements link = doc.select("link");
		assertThat(link.html(), is("".trim()));
		
		expectedFile.delete();
		
	}
	
	@Test
	public void composed_attributes_should_be_built_as_attributes_map() throws IOException {
		
		new AsciidoctorInvoker().invoke("-a", "stylesheet=mystyles.css", "-a", "linkcss", "target/test-classes/rendersample.asciidoc");
		File expectedFile = new File("target/test-classes/rendersample.html");
		
		Document doc = Jsoup.parse(expectedFile, "UTF-8");
		Elements link = doc.select("link[href]");
		String attr = link.attr("href");
		assertThat(attr, is("./mystyles.css"));
		
		expectedFile.delete();
		
	}
	
	@Test
	public void destination_dir_should_render_files_to_ouput_directory() {
		File outputDirectory = temporaryFolder.getRoot();
		
		new AsciidoctorInvoker().invoke("-D", outputDirectory.getAbsolutePath(), "target/test-classes/rendersample.asciidoc");

		assertThat(new File(outputDirectory, "rendersample.html").exists(), is(true));
		
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void no_input_file_should_throw_an_exception() {
		
		new AsciidoctorInvoker().invoke("");
		
	}

	@Test
	public void more_than_one_input_file_should_throw_an_exception() {
		
		new AsciidoctorInvoker().invoke("target/test-classes/rendersample.asciidoc", "target/test-classes/tocsample.asciidoc");
		
		File expectedRenderFile = new File("target/test-classes/rendersample.html");
        
        assertThat(expectedRenderFile.exists(), is(true));
        expectedRenderFile.delete();
		
        File expectedTocFile = new File("target/test-classes/tocsample.html");
        
        assertThat(expectedTocFile.exists(), is(true));
        expectedTocFile.delete();
        
	}
	
	@Test
	public void glob_expression_can_be_used_to_render_AsciiDoc_files() {
	    
	    new AsciidoctorInvoker().invoke("**/toc*.asciidoc");
        
        File expectedTocFile = new File("target/test-classes/tocsample.html");
        
        assertThat(expectedTocFile.exists(), is(true));
        expectedTocFile.delete();
	    
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
		
		new AsciidoctorInvoker().invoke("-o", "-", "target/test-classes/rendersample.asciidoc");
		
		Document doc = Jsoup.parse(output.toString(), "UTF-8");
		
		Elements link = doc.select("div[class]");
		
		String attr = link.attr("class");
		assertThat(attr, is("sect1"));
		
	}
	
	@Test
	public void verbose_option_should_fill_monitor_map() {
		
		ByteArrayOutputStream output = redirectStdout();
		
		new AsciidoctorInvoker().invoke("--verbose", "target/test-classes/rendersample.asciidoc");
		
		String outputConsole = output.toString();
		assertThat(outputConsole, startsWith("Time to read and parse source"));
		
	}

	private ByteArrayOutputStream redirectStdout() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output));
		return output;
	}
	
}
