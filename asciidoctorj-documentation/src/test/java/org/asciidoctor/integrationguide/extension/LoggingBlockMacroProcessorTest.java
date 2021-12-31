package org.asciidoctor.integrationguide.extension;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class LoggingBlockMacroProcessorTest {

    @ArquillianResource
    private Asciidoctor asciidoctor;

    @ArquillianResource
    private ClasspathResources classpathResources;

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
