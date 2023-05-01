package org.asciidoctor;

import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static org.asciidoctor.OptionsBuilder.options;
import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenCustomTemplatesAreUsed {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @ClasspathResource("src/custom-backends/haml/html5-tweaks")
    private File html5TweaksDir;

    @ClasspathResource("rendersample.asciidoc")
    private File renderSample;

    @Test
    public void document_should_be_rendered_using_given_template_dir() {

        Options options = options().templateDir(html5TweaksDir).toFile(false).get();
        String renderContent = asciidoctor.convertFile(renderSample, options);

        Document doc = Jsoup.parse(renderContent, "UTF-8");
        Element paragraph = doc.select("div.content").first();
        assertThat(paragraph).isNotNull();
    }

}
