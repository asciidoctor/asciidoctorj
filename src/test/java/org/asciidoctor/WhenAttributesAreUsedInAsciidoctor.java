package org.asciidoctor;

import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.asciidoctor.internal.IOUtils;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WhenAttributesAreUsedInAsciidoctor {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();
	
	private Asciidoctor asciidoctor = JRubyAsciidoctor.create();

	@Test
	public void setting_toc_attribute_table_of_contents_should_be_generated() {
		
		Attributes attributes = attributes().tableOfContents(true).get();
		Options options = options().attributes(attributes).get();
		
		String renderContent = asciidoctor.renderFile(new File("target/test-classes/tocsample.asciidoc"), options);
		
		Document doc = Jsoup.parse(renderContent, "UTF-8");
		Elements tocElement = doc.select("div.toc");
		assertThat(tocElement.hasClass("toc"), is(true));
		
	}

	@Test
	public void unsetting_toc_attribute_table_of_contents_should_not_be_generated() {
		
		Attributes attributes = attributes().tableOfContents(false).get();
		Options options = options().attributes(attributes).get();
		
		String renderContent = asciidoctor.renderFile(new File("target/test-classes/tocsample.asciidoc"), options);
		
		Document doc = Jsoup.parse(renderContent, "UTF-8");
		Elements tocElement = doc.select("div.toc");
		assertThat(tocElement.hasClass("toc"), is(false));
		
	}
	
	@Test
	public void styleSheetName_is_set_custom_stylesheet_should_be_used_() throws IOException {
		
		Attributes attributes = attributes().styleSheetName("mycustom.css").get();
		Options options = options().inPlace(false).safe(SafeMode.UNSAFE).toDir(testFolder.getRoot()).attributes(attributes).get();
		
		asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
		
		Document doc = Jsoup.parse(new File(testFolder.getRoot(), "rendersample.html"), "UTF-8");
		Elements link = doc.select("link[href]");
		String attr = link.attr("href");
		assertThat(attr, is("./mycustom.css"));
		
	}
	
	@Test
	public void unsetting_styleSheetName_should_leave_document_without_style() throws IOException {
		
		Attributes attributes = attributes().unsetStyleSheet().get();
		Options options = options().inPlace(false).safe(SafeMode.UNSAFE).toDir(testFolder.getRoot()).attributes(attributes).get();
		
		asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
		
		//String readFull = IOUtils.readFull(new FileInputStream(new File(testFolder.getRoot(), "rendersample.html")));
		Document doc = Jsoup.parse(new File(testFolder.getRoot(), "rendersample.html"), "UTF-8");
		Elements link = doc.select("link[href]");
		String attr = link.attr("href");
		assertThat(attr, is(""));
		
	}
	
	@Test
	public void styles_dir_is_set_css_routes_should_use_it() throws IOException {
		
		Attributes attributes = attributes().stylesDir("./styles").styleSheetName("mycustom.css").get();
		Options options = options().inPlace(false).safe(SafeMode.UNSAFE).toDir(testFolder.getRoot()).attributes(attributes).get();
		
		asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
		
		Document doc = Jsoup.parse(new File(testFolder.getRoot(), "rendersample.html"), "UTF-8");
		Elements link = doc.select("link[href]");
		String attr = link.attr("href");
		assertThat(attr, is("./styles/mycustom.css"));
		
	}
	
}
