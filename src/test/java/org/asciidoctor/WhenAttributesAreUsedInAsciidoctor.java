package org.asciidoctor;

import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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
	
}
