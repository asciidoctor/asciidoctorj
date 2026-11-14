package org.asciidoctor.extension;

import org.asciidoctor.nativeimage.RegisterClass;
import org.asciidoctor.nativeimage.RegisterDirectory;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith(ClasspathExtension.class)
@RegisterClass({ExtensionRegistryExecutor1.class, ExtensionRegistryExecutor2.class, ExtensionRegistryExecutor3.class})
@RegisterDirectory("serviceloadertest")
public class WhenExtensionsAreRegisteredAsServiceTest {

    static final String FILENAME_HTML = "rendersample.html";

    Asciidoctor asciidoctor;
    ClassLoader originalTCCL;

    @TempDir
    File testFolder;

    @BeforeEach
    public void setup() {
        asciidoctor = Asciidoctor.Factory.create();
        originalTCCL = Thread.currentThread().getContextClassLoader();
    }

    @AfterEach
    public void cleanup() {
        Thread.currentThread().setContextClassLoader(originalTCCL);
    }


    @Disabled("Test is ignored because currently it is not possible to register two block extensions in same instance. This may require deep changes on Asciidoctor Extensions API")
    @Test
    @DisplayName("extensions should be correctly added")
    public void extensionsShouldBeCorrectlyAdded(
            @ClasspathResource("arrows-and-boxes-example.ad") File arrowsAndBoxesExample) throws IOException {

        // when:
        //To avoid registering the same extension over and over for all tests, service is instantiated manually.
        new ArrowsAndBoxesExtension().register(asciidoctor);

        File renderedFile = new File(testFolder, FILENAME_HTML);
        Options options = Options.builder()
                .inPlace(false)
                .toFile(renderedFile)
                .safe(SafeMode.UNSAFE).build();

        asciidoctor.convertFile(arrowsAndBoxesExample, options);

        // then:
        Document doc = Jsoup.parse(renderedFile, "UTF-8");

        Element arrowsJs = doc.select("script[src=http://www.headjump.de/javascripts/arrowsandboxes.js").first();
        assertThat(arrowsJs).isNotNull();

        Element arrowsCss = doc.select("link[href=http://www.headjump.de/stylesheets/arrowsandboxes.css").first();
        assertThat(arrowsCss).isNotNull();

        Element arrowsAndBoxes = doc.select("pre[class=arrows-and-boxes").first();
        assertThat(arrowsAndBoxes).isNotNull();

    }

    @Test
    @DisplayName("the active TCCL when creating the Asciidoctor instance should be used to load extensions")
    public void theActiveTCCLWhenCreatingTheAsciidoctorInstanceShouldBeUsedToLoadExtensions(
            @ClasspathResource("serviceloadertest/1") File serviceLoaderTest1,
            @ClasspathResource("serviceloadertest/2") File serviceLoaderTest2) throws Exception {

        // when:
        URLClassLoader tccl1 = new URLClassLoader(new URL[] {serviceLoaderTest1.toURI().toURL()});
        Thread.currentThread().setContextClassLoader(tccl1);
        Asciidoctor asciidoctor1 = Asciidoctor.Factory.create();

        URLClassLoader tccl2 = new URLClassLoader(new URL[] {serviceLoaderTest2.toURI().toURL()});
        Thread.currentThread().setContextClassLoader(tccl2);
        Asciidoctor asciidoctor2 = Asciidoctor.Factory.create();

        // then:
        assertThat(ExtensionRegistryExecutor1.callCount).isEqualTo(1);
        assertThat(ExtensionRegistryExecutor1.asciidoctor).isEqualTo(asciidoctor1);

        assertThat(ExtensionRegistryExecutor2.callCount).isEqualTo(1);
        assertThat(ExtensionRegistryExecutor2.asciidoctor).isEqualTo(asciidoctor2);
    }

    @Test
    @DisplayName("an extension registry declared with both the new and the deprecated service should be called once")
    public void anExtensionRegistryDeclaredWithBothServicesShouldBeCalledOnce(
            @ClasspathResource("serviceloadertest/4") File serviceLoaderTest4) throws Exception {

        // when:
        URLClassLoader tccl = new URLClassLoader(new URL[] {serviceLoaderTest4.toURI().toURL()});
        Thread.currentThread().setContextClassLoader(tccl);
        Asciidoctor.Factory.create();

        // then:
        assertThat(ExtensionRegistryExecutor3.callCount).isEqualTo(1);
    }

}
