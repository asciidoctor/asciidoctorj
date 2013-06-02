package org.asciidoctor;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.xpath.HasXPath.hasXPath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.asciidoctor.internal.JRubyAsciidoctor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import com.google.common.io.CharStreams;

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
	public void setting_toc_attribute_and_numbered_in_string_form_table_of_contents_should_be_generated() {
		
		Attributes attributes = attributes().attributes("toc numbered").get();
		Options options = options().attributes(attributes).get();
		
		String renderContent = asciidoctor.renderFile(new File("target/test-classes/tocsample.asciidoc"), options);
		
		Document doc = Jsoup.parse(renderContent, "UTF-8");
		Elements tocElement = doc.select("div.toc");
		assertThat(tocElement.hasClass("toc"), is(true));
		
		Element tocParagraph = doc.select("a[href=#paragraphs]").first();
		assertThat(tocParagraph.text(), startsWith("1."));
	}
	
	@Test
	public void setting_toc_attribute_and_numbered_in_array_form_table_of_contents_should_be_generated() {
		
		Attributes attributes = attributes().attributes(new String[] {"toc", "numbered"}).get();
		Options options = options().attributes(attributes).get();
		
		String renderContent = asciidoctor.renderFile(new File("target/test-classes/tocsample.asciidoc"), options);
		
		Document doc = Jsoup.parse(renderContent, "UTF-8");
		Elements tocElement = doc.select("div.toc");
		assertThat(tocElement.hasClass("toc"), is(true));
		
		Element tocParagraph = doc.select("a[href=#paragraphs]").first();
		assertThat(tocParagraph.text(), startsWith("1."));
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
	
	@Test
	public void unsetting_linkcss_should_embed_css_file() throws IOException {
		
		Attributes attributes = attributes().linkCss(false).get();
		Options options = options().inPlace(false).safe(SafeMode.UNSAFE).toDir(testFolder.getRoot()).attributes(attributes).get();
		
		asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
		
		//String readFull = IOUtils.readFull(new FileInputStream(new File(testFolder.getRoot(), "rendersample.html")));
		
		Document doc = Jsoup.parse(new File(testFolder.getRoot(), "rendersample.html"), "UTF-8");
		Elements cssStyle = doc.select("style");
		assertThat(cssStyle.html(), is(not("")));
		
		Elements link = doc.select("link");
		assertThat(link.html(), is("".trim()));
		
	}
	
	@Test
	public void linkcss_should_not_embed_css_file() throws IOException {
		
		Attributes attributes = attributes().linkCss(true).get();
		Options options = options().inPlace(false).safe(SafeMode.UNSAFE).toDir(testFolder.getRoot()).attributes(attributes).get();
		
		asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
		
		Document doc = Jsoup.parse(new File(testFolder.getRoot(), "rendersample.html"), "UTF-8");
		Elements link = doc.select("link[href]");
		String attr = link.attr("href");
		assertThat(attr, is("./asciidoctor.css"));
		
	}
	
	@Test
	public void copycss_with_in_place_should_copy_css_to_rendered_directory() {
		Attributes attributes = attributes().copyCss(true).get();
		Options options = options().inPlace(true).safe(SafeMode.UNSAFE).attributes(attributes).get();
		
		asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
		
		File cssFile = new File("target/test-classes/asciidoctor.css");
		assertThat(cssFile.exists(), is(true));
		cssFile.delete();
		
	}
	
	@Test
	public void copycss_negated_with_in_place_should_not_copy_css_to_rendered_directory() {
		Attributes attributes = attributes().copyCss(false).get();
		Options options = options().inPlace(true).safe(SafeMode.UNSAFE).attributes(attributes).get();
		
		asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
		
		File cssFile = new File("target/test-classes/asciidoctor.css");
		assertThat(cssFile.exists(), is(false));
		
	}
	
	@Test
	public void copycss_and_linkcss_negated_should_not_copy_css_to_rendered_file() {
		
		Attributes attributes = attributes().copyCss(true).linkCss(false).get();
		Options options = options().inPlace(true).safe(SafeMode.UNSAFE).attributes(attributes).get();
		
		asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
		
		File cssFile = new File("target/test-classes/asciidoctor.css");
		assertThat(cssFile.exists(), is(false));
		
	}
	
	@Test
	public void copycss_with_to_file_should_copy_css_to_to_file_directory() {
	
		Attributes attributes = attributes().copyCss(true).get();
		Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "output.html")).safe(SafeMode.UNSAFE).attributes(attributes).get();
		
		asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
		
		File cssFile = new File(testFolder.getRoot(), "asciidoctor.css");
		assertThat(cssFile.exists(), is(true));
		
	}
	
	@Test
	public void copycss_with_to_dir_should_copy_css_to_to_dir_directory() {
	
		Attributes attributes = attributes().copyCss(true).get();
		Options options = options().inPlace(false).toDir(testFolder.getRoot()).safe(SafeMode.UNSAFE).attributes(attributes).get();
		
		asciidoctor.renderFile(new File("target/test-classes/rendersample.asciidoc"), options);
		
		File cssFile = new File(testFolder.getRoot(), "asciidoctor.css");
		assertThat(cssFile.exists(), is(true));
		
	}
	
	
	@Test
	public void copycss_with_render_to_file_should_copy_css_to_to_file_directory() {
	
		Attributes attributes = attributes().copyCss(true).get();
		Options options = options().inPlace(false).toFile(new File(testFolder.getRoot(), "output.html")).safe(SafeMode.UNSAFE).attributes(attributes).get();
		
		asciidoctor.render("This is Asciidoctor", options);
		
		File cssFile = new File(testFolder.getRoot(), "asciidoctor.css");
		assertThat(cssFile.exists(), is(true));
		
	}
	
	@Test
	public void copycss_with_render_to_dir_should_copy_css_to_to_dir_directory() {
	
		Attributes attributes = attributes().copyCss(true).get();
		Options options = options().inPlace(false).toDir(testFolder.getRoot()).safe(SafeMode.UNSAFE).attributes(attributes).get();
		
		asciidoctor.render("This is Asciidoctor", options);
		
		File cssFile = new File(testFolder.getRoot(), "asciidoctor.css");
		assertThat(cssFile.exists(), is(true));
		
	}
	
	@Test
	public void string_content_with_icons_enabled_should_be_rendered() throws IOException, SAXException, ParserConfigurationException {
		
		InputStream content = new FileInputStream("target/test-classes/documentwithnote.asciidoc");
		
		Map<String, Object> attributes = attributes().icons(Attributes.ORIGINAL_ADMONITION_ICONS_WITH_IMG).asMap();
		Map<String, Object> options = options()
										.attributes(attributes)
									  .asMap();
		
		String result = asciidoctor.render(toString(content), options);
        result = result.replaceAll("<img(.*?)>", "<img$1/>");
		assertRenderedAdmonitionIcon(result);
		
	}
	
	@Test
	public void string_content_with_fontawesome_icons_enabled_should_be_rendered() throws IOException, SAXException, ParserConfigurationException {
		
		InputStream content = new FileInputStream("target/test-classes/documentwithnote.asciidoc");
		
		Map<String, Object> attributes = attributes().icons(Attributes.FONTAWESOME_ADMONITION_ICONS).asMap();
		Map<String, Object> options = options()
										.attributes(attributes)
									  .asMap();
		
		String result = asciidoctor.render(toString(content), options);
		assertRenderedFontAwesomeAdmonitionIcon(result);
		
	}
	
	@Test
	public void string_content_with_icons_enabled_and_iconsdir_set_should_be_rendered_with_iconsdir() throws IOException, SAXException, ParserConfigurationException {
		
		InputStream content = new FileInputStream("target/test-classes/documentwithnote.asciidoc");
		
		Map<String, Object> attributes = attributes().icons(Attributes.ORIGINAL_ADMONITION_ICONS_WITH_IMG).iconsDir("icons").asMap();
		Map<String, Object> options = options()
										.attributes(attributes)
									  .asMap();
		
		String renderContent = asciidoctor.render(toString(content), options);
		
		Document doc = Jsoup.parse(renderContent, "UTF-8");
		Elements image = doc.select("img[src]");
		String srcValue = image.attr("src");
		assertThat(srcValue, is("icons/note.png"));
		
	}
	
	@Test
	public void linkattrs_should_make_asciidoctor_render_link_macro_attributes() {
		
		Attributes attributes = attributes().linkAttrs(true).get();
		Options options = options().attributes(attributes).get();
		
		String content = asciidoctor.render("http://google.com[Google, window=\"_blank\"]", options);
		
		Document doc = Jsoup.parse(content);
		Elements image = doc.select("a[target]");
		
		String targetValue = image.attr("target");
		assertThat(targetValue, is("_blank"));
		
	}
	
	@Test
	public void experimental_flag_should_enable_experimental_features_like_keyboard_shortcuts() {
		
		Attributes attributes = attributes().experimental(true).get();
		Options options = options().attributes(attributes).get();
		
		String content = asciidoctor.render("kbd:[F11]", options);
		
		Document doc = Jsoup.parse(content);
		Elements image = doc.select("kbd");
		
		assertThat(image.text(), is("F11"));
	}
	
	private void assertRenderedFontAwesomeAdmonitionIcon(String renderContent) throws IOException, SAXException, ParserConfigurationException {
		
		Source renderFileSource = new DOMSource(inputStream2Document(new ByteArrayInputStream(renderContent.getBytes())));
		assertThat(renderFileSource, hasXPath("//i[@class='icon-note']"));
	}
	
	private void assertRenderedAdmonitionIcon(String render_content) throws IOException, SAXException, ParserConfigurationException {
		Source renderFileSource = new DOMSource(inputStream2Document(new ByteArrayInputStream(render_content.getBytes())));
		
		assertThat(renderFileSource, hasXPath("//img[@alt='Note']"));
	}
	
	private static String toString(InputStream inputStream) throws IOException {
		return CharStreams.toString( new InputStreamReader( inputStream ));
	}
	
	private static org.w3c.dom.Document inputStream2Document(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
	    DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
	    newInstance.setNamespaceAware(true);
	    org.w3c.dom.Document parse = newInstance.newDocumentBuilder().parse(inputStream);
	    return parse;
	}
	
}
