package org.asciidoctor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@RunAsClient
public class WildflyIntegrationTest {

    @Deployment
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(AsciidoctorServlet.class)
                .setManifest(new File("src/test/resources/MANIFEST.MF"));
    }

    @ArquillianResource
    private URL url;

    @Test
    public void test() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url, "asciidoctor").openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.getOutputStream().write("Hello World".getBytes());

        try (InputStream in = conn.getInputStream()) {
            final Document doc = Jsoup.parse(readFull(in));
            final Element first = doc.body().children().first();
            assertThat(first.tagName()).isEqualTo("div");
            assertThat(first.className()).isEqualTo("paragraph");
            final Element paragraph = first.children().first();
            assertThat(paragraph.tagName()).isEqualTo("p");
            assertThat(paragraph.ownText()).isEqualTo("Hello World");
        }
    }

    public static String readFull(InputStream inputStream) {
        return new Scanner(inputStream).useDelimiter("\\A").next();
    }

}
