package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class TerminalCommandTreeprocessorTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("treeprocessorresult.adoc")
    private File referenceDocument;

    @ClasspathResource("treeprocessorcontent.adoc")
    private File treeprocessorContent;


    @Test
    public void should_process_terminal_listings() {

        String referenceResult = asciidoctor.convertFile(
                referenceDocument,
                Options.builder()
                        .standalone(false)
                        .toFile(false)
                        .build());

//tag::include[]
        File src = //...
//end::include[]
                treeprocessorContent;

//tag::include[]
        asciidoctor.javaExtensionRegistry()
                .treeprocessor(TerminalCommandTreeprocessor.class); // <1>
        String result = asciidoctor.convertFile(
                src,
                Options.builder()
                        .standalone(false)
                        .toFile(false)
                        .build());
//end::include[]

        assertThat(result, is(referenceResult));
    }

    @Test
    public void should_process_terminal_listings_after_registering_via_extension_registry(
            @ClasspathResource("extensionregistry") File extensionRegistryDir) throws MalformedURLException {

        String referenceResult = asciidoctor.convertFile(
                referenceDocument,
                Options.builder()
                        .standalone(false)
                        .toFile(false)
                        .build());

//tag::include-extension-registry[]
        File src = //...
//end::include-extension-registry[]
                treeprocessorContent;


        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

        try {
            URL serviceDir = extensionRegistryDir.toURI().toURL();
            URLClassLoader tccl = new URLClassLoader(new URL[]{serviceDir});
            Thread.currentThread().setContextClassLoader(tccl);

            Asciidoctor asciidoctor = Asciidoctor.Factory.create();
//tag::include-extension-registry[]
            String result = asciidoctor.convertFile(
                    src,
                    Options.builder()
                            .standalone(false)
                            .toFile(false)
                            .build());
//end::include-extension-registry[]
            assertThat(result, is(referenceResult));
        } finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }

}
