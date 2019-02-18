package org.asciidoctor.extension;

import org.asciidoctor.SafeMode;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;

import static java.util.Collections.singletonList;
import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class WhenRubyExtensionIsRegistered {

    @ArquillianResource
    private ClasspathResources classpath;

    @ArquillianResource(Unshared.class)
    private AsciidoctorJRuby asciidoctor;

    @Test
    public void ruby_block_processor_should_be_registered_with_explicit_block_name() {
        
        RubyExtensionRegistry rubyExtensionRegistry = asciidoctor.rubyExtensionRegistry();
        rubyExtensionRegistry.loadClass(getClass().getResourceAsStream("/ruby-extensions/YellRubyBlock.rb")).block("rubyyell", "YellRubyBlock");

        String content = asciidoctor.convert(
                "= Block Yell Example\n" +
                    "\n" +
                    "content\n" +
                    "\n" +
                    "[rubyyell]\n" +
                    "The time is now. Get a move on.",
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(2));
        assertThat(elements.get(1).text(), is("THE TIME IS NOW! GET A MOVE ON!"));
        
    }

    @Test
    public void ruby_block_processor_should_be_registered_with_implicit_block_name() {

        RubyExtensionRegistry rubyExtensionRegistry = asciidoctor.rubyExtensionRegistry();
        rubyExtensionRegistry.loadClass(getClass().getResourceAsStream("/ruby-extensions/YellRubyBlock.rb")).block("YellRubyBlock");

        String content = asciidoctor.convert(
            "= Block Yell Example\n" +
                "\n" +
                "content\n" +
                "\n" +
                "[yell]\n" +
                "The time is now. Get a move on.",
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(2));
        assertThat(elements.get(1).text(), is("THE TIME IS NOW! GET A MOVE ON!"));

    }

    @Test
    public void ruby_block_macro_processor_should_be_registered_with_block_name() {

        RubyExtensionRegistry rubyExtensionRegistry = asciidoctor.rubyExtensionRegistry();
        rubyExtensionRegistry.loadClass(getClass().getResourceAsStream("/ruby-extensions/gist-block-macro.rb")).blockMacro("mygist", "GistBlockMacro");

        String content = asciidoctor.convert(
                ".My Gist\n" +
                    "mygist::123456[]",
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByTag("script");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).attr("src"), is("https://gist.github.com/123456.js"));

    }

    @Test
    public void ruby_block_macro_processor_should_be_registered_with_implicit_block_name() {

        RubyExtensionRegistry rubyExtensionRegistry = asciidoctor.rubyExtensionRegistry();
        rubyExtensionRegistry.loadClass(getClass().getResourceAsStream("/ruby-extensions/gist-block-macro.rb")).blockMacro("GistBlockMacro");

        String content = asciidoctor.convert(
                ".My Gist\n" +
                    "gist::42[]",
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByTag("script");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).attr("src"), is("https://gist.github.com/42.js"));

    }

    @Test
    public void ruby_inline_macro_processor_should_be_registered_with_macro_name() {

        RubyExtensionRegistry rubyExtensionRegistry = asciidoctor.rubyExtensionRegistry();
        rubyExtensionRegistry.loadClass(getClass().getResourceAsStream("/ruby-extensions/man-inline-macro.rb")).inlineMacro("myman", "ManInlineMacro");

        String content = asciidoctor.convert(
                "= Man Inline Macro Extension\n" +
                    "\n" +
                    "See myman:gittutorial[7] to get started.",
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByTag("a");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).attr("href"), is("gittutorial.html"));

    }

    @Test
    public void ruby_inline_macro_processor_should_be_registered_with_implicit_macro_name() {

        RubyExtensionRegistry rubyExtensionRegistry = asciidoctor.rubyExtensionRegistry();
        rubyExtensionRegistry.loadClass(getClass().getResourceAsStream("/ruby-extensions/man-inline-macro.rb"))
            .inlineMacro("ManInlineMacro");

        String content = asciidoctor.convert(
            "= Man Inline Macro Extension\n" +
                "\n" +
                "See man:dockertutorial[7] to get started.",
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByTag("a");
        assertThat(elements.size(), is(1));
        assertThat(elements.get(0).attr("href"), is("dockertutorial.html"));

    }

    @Test
    public void ruby_treeprocessor_should_be_registered() {

        final String rubyExtPath = classpath.getResource("ruby-extensions").getAbsolutePath();
        final AsciidoctorJRuby asciidoctor = AsciidoctorJRuby.Factory.create(singletonList(rubyExtPath));
        asciidoctor.rubyExtensionRegistry()
            .requireLibrary("shell-session-tree-processor.rb")
            .treeprocessor("ShellSessionTreeProcessor");

        String content = asciidoctor.convert(
            " $ echo \"Hello, World!\"\n" +
                " > Hello, World!\n" +
                "\n" +
                " $ gem install asciidoctor",
                options().toFile(false).get());

        final Document document = Jsoup.parse(content);
        final TextNode commandElement = document.getElementsByClass("command").get(0).textNodes().get(0);
        assertThat(commandElement.getWholeText(), is("echo \"Hello, World!\""));
        final TextNode commandElement2 = document.getElementsByClass("command").get(1).textNodes().get(0);
        assertThat(commandElement2.getWholeText(), is("gem install asciidoctor"));
    }

    @Test
    public void ruby_includeprocessor_should_be_registered() {
        asciidoctor.rubyExtensionRegistry()
            .loadClass(getClass().getResourceAsStream("/ruby-extensions/response-include-processor.rb"))
            .includeProcessor("ResponseIncludeProcessor");

        String content = asciidoctor.convert(
            "The response to everything is\n" +
                "\n" +
                "include::response[]" +
                "",
            options().toFile(false).safe(SafeMode.SAFE).get());

        final Document document = Jsoup.parse(content);
        assertThat(
            document.getElementsByClass("paragraph").get(1).getElementsByTag("p").get(0).toString(),
            is("<p>42</p>"));
    }

    @Test
    public void ruby_postprocessor_should_be_registered() {

        final String rubyExtPath = classpath.getResource("ruby-extensions").getAbsolutePath();
        final AsciidoctorJRuby asciidoctor = AsciidoctorJRuby.Factory.create(singletonList(rubyExtPath));
        asciidoctor.rubyExtensionRegistry()
            .requireLibrary("xml-entity-postprocessor.rb")
            .postprocessor("XmlEntityPostprocessor");

        String content = asciidoctor.convert(
            "Read &sect;2 and it&apos;ll all be clear.",
                options().toFile(false).get());

        assertThat(content, containsString("Read &#167;2 and it&#39;ll all be clear."));
    }

    @Test
    public void ruby_preprocessor_should_be_registered() {

        final String rubyExtPath = classpath.getResource("ruby-extensions").getAbsolutePath();
        final AsciidoctorJRuby asciidoctor = AsciidoctorJRuby.Factory.create(singletonList(rubyExtPath));
        asciidoctor.rubyExtensionRegistry()
            .requireLibrary("front-matter-preprocessor.rb")
            .preprocessor("FrontMatterPreprocessor");

        String content = asciidoctor.convert(
            "---\n" +
                "tags: [announcement, website]\n" +
                "---\n" +
                "= Document Title\n" +
                "\n" +
                "content\n" +
                "\n" +
                "[subs=\"attributes,specialcharacters\"]\n" +
                ".Captured front matter\n" +
                "....\n" +
                "---\n" +
                "{front-matter}\n" +
                "---\n" +
                "....",
                options().toFile(false).get());

        final Document document = Jsoup.parse(content);
        final Element contentElement = document.getElementsByClass("content").get(0);
        final Element literalElement = contentElement.getElementsByTag("pre").get(0);
        assertThat(literalElement.toString().replace("\r", ""),
            containsString("---\n" +
                "tags: [announcement, website]\n" +
                "---"));
    }

    @Test
    public void ruby_docinfoprocessor_should_be_registered() {

        final String rubyExtPath = classpath.getResource("ruby-extensions").getAbsolutePath();
        final AsciidoctorJRuby asciidoctor = AsciidoctorJRuby.Factory.create(singletonList(rubyExtPath));
        asciidoctor.rubyExtensionRegistry()
            .requireLibrary("view-result-docinfoprocessor.rb")
            .docinfoProcessor("ViewResultDocinfoProcessor");

        String content = asciidoctor.convert(
            "= View Result Sample             \n" +
                "                                 \n" +
                ".This will have a link next to it\n" +
                "----                             \n" +
                "* always displayed               \n" +
                "* always displayed 2             \n" +
                "----                             \n" +
                "                                 \n" +
                "[.result]                        \n" +
                "====                             \n" +
                "* hidden till clicked            \n" +
                "* hidden till clicked 2          \n" +
                "====                             ",
                options().toFile(false).safe(SafeMode.SAFE).headerFooter(true).get());

        final Document document = Jsoup.parse(content);
        final Iterator<Element> elems = document.getElementsByTag("style").iterator();
        boolean found = false;
        while (elems.hasNext()) {
            final Element styleElem = elems.next();
            if (styleElem.toString().contains(".listingblock a.view-result")) {
                found = true;
            }
        }
        assertTrue("Could not find style element that should have been added by docinfo processor:\n" + document, found);
    }
}
