package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.util.ClasspathHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class LoggingBlockMacroProcessorTest {

    private Asciidoctor asciidoctor;
    private ClasspathHelper classpathResources;

    @BeforeEach
    public void beforeEach() {
        asciidoctor = Asciidoctor.Factory.create();
        classpathResources = new ClasspathHelper();
        classpathResources.setClassloader(this.getClass());
    }

    @Test
    public void should_log_from_extension() {

//tag::include[]
        File loggingmacro_adoc = // ...
//end::include[]
                classpathResources.getResource("logging-macro.adoc");
//tag::include[]
        List<LogRecord> logRecords = new ArrayList<>();
        asciidoctor.registerLogHandler(logRecords::add);
        asciidoctor.javaExtensionRegistry().blockMacro(LoggingBlockMacroProcessor.class);      // <1>

        asciidoctor.convertFile(loggingmacro_adoc,
                Options.builder()
                        .sourcemap(true)
                        .toFile(false)
                        .build());

        assertEquals(1, logRecords.size());
        assertTrue(logRecords.get(0).getCursor().getFile().endsWith("logging-macro.adoc"));
        assertEquals(3, logRecords.get(0).getCursor().getLineNumber());
        assertEquals("HelloWorld", logRecords.get(0).getMessage());
//end::include[]
    }

}
