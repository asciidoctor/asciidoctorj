package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.jruby.internal.IOUtils;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class ASTShowcase {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

    @Test
    public void createAstStructure() throws Exception {

        asciidoctor.javaExtensionRegistry().treeprocessor(ASTExtractorTreeprocessor.class);

        asciidoctor.loadFile(classpathResources.getResource("ast-demo.adoc"), OptionsBuilder.options().asMap());

        assertThat(
                ASTExtractorTreeprocessor.result.toString(),
                is(IOUtils.readFull(new FileReader(classpathResources.getResource("ast-demo-result.txt"))).replaceAll("\\r", "")));
    }

}
