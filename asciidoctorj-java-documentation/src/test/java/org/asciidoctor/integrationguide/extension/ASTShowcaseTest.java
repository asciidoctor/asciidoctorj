package org.asciidoctor.integrationguide.extension;

import static org.assertj.core.api.Assertions.assertThat;

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
import java.nio.file.Files;
import java.nio.file.Path;


@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class ASTShowcaseTest {

    @AsciidoctorInstance
    private Asciidoctor asciidoctor;

    @ClasspathResource("ast-demo.adoc")
    private File astDemo;

    @ClasspathResource("ast-demo-result.txt")
    private Path astDemoResult;


    @Test
    public void createAstStructure() throws Exception {

        asciidoctor.javaExtensionRegistry().treeprocessor(ASTExtractorTreeprocessor.class);

        asciidoctor.loadFile(astDemo, Options.builder().build());

        assertThat(ASTExtractorTreeprocessor.result.toString()).isEqualTo(Files.readString(astDemoResult).replaceAll("\\r", ""));
    }

}
