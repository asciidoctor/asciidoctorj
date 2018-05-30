package org.asciidoctor.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.arquillian.api.Unshared;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

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

        String content = asciidoctor.render(
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

        String content = asciidoctor.render(
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
            String contentWithoutBlock = asciidoctor.renderFile(
                classpath.getResource("sample-with-ruby-yell-block.ad"),
                options().toFile(false).get());

            Document docWithoutBlock = Jsoup.parse(contentWithoutBlock, "UTF-8");
            Elements elementsWithoutBlock = docWithoutBlock.getElementsByClass("paragraph");
            assertThat(elementsWithoutBlock.size(), is(2));
            assertThat(elementsWithoutBlock.get(1).text(), not(is("THE TIME IS NOW! GET A MOVE ON!")));
        }
        {
            extensionGroup.register();
            String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-ruby-yell-block.ad"),
                options().toFile(false).get());

            Document doc = Jsoup.parse(content, "UTF-8");
            Elements elements = doc.getElementsByClass("paragraph");
            assertThat(elements.size(), is(2));
            assertThat(elements.get(1).text(), is("THE TIME IS NOW! GET A MOVE ON!"));
        }
        {
            extensionGroup.unregister();

            String contentWithoutBlock = asciidoctor.renderFile(
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

        String content = asciidoctor.render(
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

        String content = asciidoctor.render(
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

        String content = asciidoctor.render(
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

        String content = asciidoctor.render(
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
    public void ruby_postprocessor_should_be_registered() {

        final String rubyExtPath = classpath.getResource("ruby-extensions").getAbsolutePath();
        final Asciidoctor asciidoctor = Asciidoctor.Factory.create(Arrays.asList(rubyExtPath));
        asciidoctor.createGroup()
            .requireRubyLibrary("xml-entity-postprocessor.rb")
            .rubyPostprocessor("XmlEntityPostprocessor")
            .register();

        String content = asciidoctor.render(
            "Read &sect;2 and it&apos;ll all be clear.",
            options().toFile(false).get());

        System.out.println(content);
        assertThat(content, containsString("Read &#167;2 and it&#39;ll all be clear."));
    }

}
