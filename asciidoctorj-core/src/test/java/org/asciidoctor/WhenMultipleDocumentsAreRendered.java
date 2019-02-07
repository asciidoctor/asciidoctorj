package org.asciidoctor;

import org.asciidoctor.jruby.internal.IOUtils;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.util.ClasspathResources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.asciidoctor.OptionsBuilder.options;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WhenMultipleDocumentsAreRendered {

    @Rule
    public ClasspathResources classpath = new ClasspathResources();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private Asciidoctor asciidoctor;

    @Before
    public void before() {
        asciidoctor = JRubyAsciidoctor.create();
    }

    @Test
    public void should_render_the_same_document_a_100_times() throws FileNotFoundException {
//        final MemoryLogHandler memoryLogHandler = registerMemoryLogHandler();

        final File inputFile = classpath.getResource("documenttitle.adoc");
        int i = 0;
        String renderContent = null;
        for (; i < 100; i++) {
            renderContent = asciidoctor.convert(IOUtils.readFull(new FileInputStream(inputFile)),
                    options()
                            .safe(SafeMode.UNSAFE)
                            .asMap());
        }

        assertThat(i, equalTo(100));
        assertThat(renderContent, not(isEmptyOrNullString()));
    }

}
