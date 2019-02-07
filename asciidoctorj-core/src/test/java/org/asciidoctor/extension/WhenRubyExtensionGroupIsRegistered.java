package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.SafeMode;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class WhenRubyExtensionGroupIsRegistered {

    @ArquillianResource
    private ClasspathResources classpath;

    @ArquillianResource(Unshared.class)
    private Asciidoctor asciidoctor;

    @Test
    public void ruby_extension_should_be_registered_with_explicit_block_name() {
        
        this.asciidoctor.createGroup()
            .loadRubyClass(getClass().getResourceAsStream("/ruby-extensions/YellRubyBlock.rb")).rubyBlock("rubyyell", "YellRubyBlock")
            .register();

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
    public void ruby_extension_should_be_registered_with_implicit_block_name() {

        this.asciidoctor.createGroup()
            .loadRubyClass(getClass().getResourceAsStream("/ruby-extensions/YellRubyBlock.rb")).rubyBlock("YellRubyBlock")
            .register();

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
    public void ruby_extension_should_be_unregistered() {

        ExtensionGroup extensionGroup = this.asciidoctor.createGroup()
            .loadRubyClass(getClass().getResourceAsStream("/ruby-extensions/YellRubyBlock.rb"))
            .rubyBlock("rubyyell", "YellRubyBlock");

        {
            String contentWithoutBlock = asciidoctor.convertFile(
                classpath.getResource("sample-with-ruby-yell-block.ad"),
                options().toFile(false).get());

            Document docWithoutBlock = Jsoup.parse(contentWithoutBlock, "UTF-8");
            Elements elementsWithoutBlock = docWithoutBlock.getElementsByClass("paragraph");
            assertThat(elementsWithoutBlock.size(), is(2));
            assertThat(elementsWithoutBlock.get(1).text(), not(is("THE TIME IS NOW! GET A MOVE ON!")));
        }
        {
            extensionGroup.register();
            String content = asciidoctor.convertFile(
                classpath.getResource("sample-with-ruby-yell-block.ad"),
                options().toFile(false).get());

            Document doc = Jsoup.parse(content, "UTF-8");
            Elements elements = doc.getElementsByClass("paragraph");
            assertThat(elements.size(), is(2));
            assertThat(elements.get(1).text(), is("THE TIME IS NOW! GET A MOVE ON!"));
        }
        {
            extensionGroup.unregister();

            String contentWithoutBlock = asciidoctor.convertFile(
                classpath.getResource("sample-with-ruby-yell-block.ad"),
                options().toFile(false).get());

            Document docWithoutBlock = Jsoup.parse(contentWithoutBlock, "UTF-8");
            Elements elementsWithoutBlock = docWithoutBlock.getElementsByClass("paragraph");
            assertThat(elementsWithoutBlock.size(), is(2));
            assertThat(elementsWithoutBlock.get(1).text(), not(is("THE TIME IS NOW! GET A MOVE ON!")));
        }
    }


    @Test
    public void ruby_block_macro_processor_should_be_registered_with_block_name() {

        this.asciidoctor.createGroup()
            .loadRubyClass(getClass().getResourceAsStream("/ruby-extensions/gist-block-macro.rb")).rubyBlockMacro("mygist", "GistBlockMacro")
            .register();

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

        this.asciidoctor.createGroup()
            .loadRubyClass(getClass().getResourceAsStream("/ruby-extensions/gist-block-macro.rb")).rubyBlockMacro("GistBlockMacro")
            .register();

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

        this.asciidoctor.createGroup()
            .loadRubyClass(getClass().getResourceAsStream("/ruby-extensions/man-inline-macro.rb")).rubyInlineMacro("myman", "ManInlineMacro")
            .register();

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

        this.asciidoctor.createGroup()
            .loadRubyClass(getClass().getResourceAsStream("/ruby-extensions/man-inline-macro.rb")).rubyInlineMacro("ManInlineMacro")
            .register();

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

        this.asciidoctor.createGroup()
            .loadRubyClass(getClass().getResourceAsStream("/ruby-extensions/shell-session-tree-processor.rb"))
            .rubyTreeprocessor("ShellSessionTreeProcessor")
            .register();

        String content = this.asciidoctor.convert(
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
    public void ruby_postprocessor_should_be_registered() {

        final String rubyExtPath = classpath.getResource("ruby-extensions").getAbsolutePath();
        final Asciidoctor asciidoctor = JRubyAsciidoctor.create(singletonList(rubyExtPath));
        asciidoctor.createGroup()
            .requireRubyLibrary("xml-entity-postprocessor.rb")
            .rubyPostprocessor("XmlEntityPostprocessor")
            .register();

        String content = asciidoctor.convert(
            "Read &sect;2 and it&apos;ll all be clear.",
            options().toFile(false).get());

        System.out.println(content);
        assertThat(content, containsString("Read &#167;2 and it&#39;ll all be clear."));
    }

    @Test
    public void ruby_includeprocessor_should_be_registered() {
        asciidoctor.createGroup()
            .loadRubyClass(getClass().getResourceAsStream("/ruby-extensions/response-include-processor.rb"))
            .rubyIncludeProcessor("ResponseIncludeProcessor")
            .register();

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
    public void ruby_preprocessor_should_be_registered() {

        this.asciidoctor.createGroup()
            .loadRubyClass(getClass().getResourceAsStream("/ruby-extensions/front-matter-preprocessor.rb"))
            .rubyPreprocessor("FrontMatterPreprocessor")
            .register();

        String content = this.asciidoctor.convert(
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
        final Asciidoctor asciidoctor = JRubyAsciidoctor.create(singletonList(rubyExtPath));
        asciidoctor.createGroup()
            .requireRubyLibrary("view-result-docinfoprocessor.rb")
            .rubyDocinfoProcessor("ViewResultDocinfoProcessor")
            .register();

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
