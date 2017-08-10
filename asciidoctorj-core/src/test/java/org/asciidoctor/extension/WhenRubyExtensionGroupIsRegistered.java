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

import static org.asciidoctor.OptionsBuilder.options;
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
    public void ruby_extension_should_be_registered() {
        
        this.asciidoctor.createGroup()
            .loadRubyClass(Class.class.getResourceAsStream("/YellRubyBlock.rb")).rubyBlock("rubyyell", "YellRubyBlock")
            .register();

        String content = asciidoctor.renderFile(
                classpath.getResource("sample-with-ruby-yell-block.ad"),
                options().toFile(false).get());

        Document doc = Jsoup.parse(content, "UTF-8");
        Elements elements = doc.getElementsByClass("paragraph");
        assertThat(elements.size(), is(2));
        assertThat(elements.get(1).text(), is("THE TIME IS NOW! GET A MOVE ON!"));
        
    }

    @Test
    public void ruby_extension_should_be_unregistered() {

        ExtensionGroup extensionGroup = this.asciidoctor.createGroup()
            .loadRubyClass(Class.class.getResourceAsStream("/YellRubyBlock.rb"))
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

}
