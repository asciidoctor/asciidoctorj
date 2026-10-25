package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.ast.Cursor;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Reader;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;
import org.asciidoctor.log.TestLogHandlerService;
import org.asciidoctor.test.AsciidoctorInstance;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.asciidoctor.test.AsciidoctorInstance.InstanceScope.PER_METHOD;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenAsciidoctorLogsToConsoleTest {

    @AsciidoctorInstance(scope = PER_METHOD)
    private Asciidoctor asciidoctor;

    @ClasspathResource("documentwithnotexistingfile.adoc")
    private File documentWithNotExistingFile;

    @ClasspathResource("documentwithinvalidrefs.adoc")
    private File documentWithInvalidRefs;

    @TempDir
    private File tempFolder;


    @BeforeEach
    public void before() {
        TestLogHandlerService.clear();
    }

    @AfterEach
    public void cleanup() throws IOException {
        LogManager.getLogManager().readConfiguration();
        TestLogHandlerService.clear();
    }

    @Test
    public void shouldRedirectToJUL() {
        final MemoryLogHandler memoryLogHandler = registerMemoryLogHandler();

        File inputFile = documentWithNotExistingFile;
        asciidoctor.convertFile(inputFile,
                Options.builder()
                        .inPlace(true)
                        .safe(SafeMode.SERVER)
                        .attributes(Attributes.builder().allowUriRead(true).build())
                        .build());

        File expectedFile = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile.delete();

        assertThat(memoryLogHandler.getLogRecords().size()).isEqualTo(4);
        assertThat(memoryLogHandler.getLogRecords().get(0).getMessage())
                .contains("include file not found")
                .contains("documentwithnotexistingfile.adoc: line 3");
    }

    private MemoryLogHandler registerMemoryLogHandler() {
        final Logger logger = Logger.getLogger("asciidoctor");
        final MemoryLogHandler handler = new MemoryLogHandler();
        logger.addHandler(handler);
        return handler;
    }

    @Test
    public void shouldNotifyLogHandler() {

        final List<LogRecord> logRecords = new ArrayList<>();

        asciidoctor.registerLogHandler(logRecords::add);

        File inputFile = documentWithNotExistingFile;
        asciidoctor.convertFile(inputFile,
                Options.builder()
                        .inPlace(true)
                        .safe(SafeMode.SERVER)
                        .attributes(Attributes.builder().allowUriRead(true).build())
                        .build());

        File expectedFile = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile.delete();

        assertThat(logRecords.size()).isEqualTo(4);
        assertThat(logRecords.get(0).getMessage()).contains("include file not found");
        final Cursor cursor = logRecords.get(0).getCursor();
        assertThat(cursor.getDir().replace('\\', '/')).isEqualTo(inputFile.getParent().replace('\\', '/'));
        assertThat(cursor.getFile()).isEqualTo(inputFile.getName());
        assertThat(cursor.getLineNumber()).isEqualTo(3);

        for (LogRecord logRecord : logRecords) {
            assertThat(logRecord.getCursor()).isNotNull();
            assertThat(logRecord.getCursor().getFile()).isNotNull();
            assertThat(logRecord.getCursor().getDir()).isNotNull();
        }

    }

    @Test
    public void shouldLogInvalidRefs() {

        final List<LogRecord> logRecords = new ArrayList<>();

        asciidoctor.registerLogHandler(logRecords::add);

        File inputFile = documentWithInvalidRefs;
        asciidoctor.convertFile(inputFile,
                Options.builder()
                        .inPlace(true)
                        .safe(SafeMode.SERVER)
                        .toFile(false)
                        .attributes(Attributes.builder().allowUriRead(true).build())
                        .build());

        assertThat(logRecords).hasSize(1);
        assertThat(logRecords.get(0).getMessage()).contains("invalid reference: invalidref");
        final Cursor cursor = logRecords.get(0).getCursor();
        assertThat(cursor).isNull();
    }

    @Test
    public void shouldOnlyNotifyFromRegisteredAsciidoctor() {

        final List<LogRecord> logRecords = new ArrayList<>();

        final Asciidoctor secondInstance = Asciidoctor.Factory.create();

        asciidoctor.registerLogHandler(logRecords::add);

        // Now render via second instance and check that there is no notification
        File inputFile = documentWithNotExistingFile;
        secondInstance.convertFile(inputFile,
                Options.builder()
                        .inPlace(true)
                        .safe(SafeMode.SERVER)
                        .attributes(Attributes.builder().allowUriRead(true).build())
                        .build());

        File expectedFile1 = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile1.delete();

        assertThat(logRecords.size()).isEqualTo(0);

        // Now render via first instance and check that notifications appeared.
        asciidoctor.convertFile(inputFile,
                Options.builder()
                        .inPlace(true)
                        .safe(SafeMode.SERVER)
                        .attributes(Attributes.builder().allowUriRead(true).build())
                        .build());

        File expectedFile2 = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile2.delete();

        assertThat(logRecords.size()).isEqualTo(4);
        assertThat(logRecords.get(0).getMessage()).contains("include file not found");
        final Cursor cursor = logRecords.get(0).getCursor();
        assertThat(cursor.getDir().replace('\\', '/')).isEqualTo(inputFile.getParent().replace('\\', '/'));
        assertThat(cursor.getFile()).isEqualTo(inputFile.getName());
        assertThat(cursor.getLineNumber()).isEqualTo(3);
    }

    @Test
    public void shouldNoLongerNotifyAfterUnregisterOnlyNotifyFromRegisteredAsciidoctor() {

        final List<LogRecord> logRecords = new ArrayList<>();

        final LogHandler logHandler = logRecords::add;
        asciidoctor.registerLogHandler(logHandler);

        File inputFile = documentWithNotExistingFile;
        asciidoctor.convertFile(inputFile,
                Options.builder()
                        .inPlace(true)
                        .safe(SafeMode.SERVER)
                        .attributes(Attributes.builder().allowUriRead(true).build())
                        .build());

        File expectedFile = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile.delete();

        assertThat(logRecords.size()).isEqualTo(4);
        logRecords.clear();

        asciidoctor.unregisterLogHandler(logHandler);

        asciidoctor.convertFile(inputFile,
                Options.builder()
                        .inPlace(true)
                        .safe(SafeMode.SERVER)
                        .attributes(Attributes.builder().allowUriRead(true).build())
                        .build());

        File expectedFile2 = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile2.delete();
        assertThat(logRecords.size()).isEqualTo(0);
    }

    @Test
    public void shouldNotifyLogHandlerService() {

        File inputFile = documentWithNotExistingFile;
        asciidoctor.convertFile(inputFile,
                Options.builder()
                        .inPlace(true)
                        .safe(SafeMode.SERVER)
                        .attributes(Attributes.builder().allowUriRead(true).build())
                        .build());

        File expectedFile = new File(inputFile.getParent(), "documentwithnotexistingfile.html");
        expectedFile.delete();

        final List<LogRecord> logRecords = TestLogHandlerService.getLogRecords();
        for (LogRecord logRecord : logRecords) {
            System.err.println(">> " + logRecord.getMessage());
        }
        assertThat(logRecords).hasSize(4);
        assertThat(logRecords.get(0).getMessage()).contains("include file not found");
        final Cursor cursor = logRecords.get(0).getCursor();
        assertThat(cursor.getDir().replace('\\', '/')).isEqualTo(inputFile.getParent().replace('\\', '/'));
        assertThat(cursor.getFile()).isEqualTo(inputFile.getName());
        assertThat(cursor.getLineNumber()).isEqualTo(3);

        for (LogRecord logRecord : logRecords) {
            assertThat(logRecord.getCursor()).isNotNull();
            assertThat(logRecord.getCursor().getFile()).isNotNull();
            assertThat(logRecord.getCursor().getDir()).isNotNull();
        }
    }

    @Name("big")
    public static class LoggingProcessor extends BlockProcessor {

        @Override
        public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
            log(new LogRecord(Severity.INFO, parent.getSourceLocation(), "Hello Log"));
            final List<String> strings = reader.readLines().stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

            return createBlock(parent, "paragraph", strings);
        }
    }

    @Test
    public void a_extension_should_be_able_to_log() {

        final List<LogRecord> logRecords = new ArrayList<>();

        asciidoctor.registerLogHandler(logRecords::add);
        asciidoctor.javaExtensionRegistry().block(LoggingProcessor.class);

        String renderContent = asciidoctor.convert("= Test\n\n== Something different\n\n[big]\nHello World",
                Options.builder().option("sourcemap", "true").build());

        assertThat(logRecords.size()).isEqualTo(1);
        assertThat(logRecords.get(0).getMessage()).isEqualTo("Hello Log");
        final Cursor cursor = logRecords.get(0).getCursor();
        assertThat(cursor.getLineNumber()).isEqualTo(3);

        assertThat(renderContent).contains("HELLO WORLD");
    }

    @Test
    public void should_fail_convert_when_logHandler_throws_an_exception() {
        // given
        final String errorMessage = "Conversion aborted by LogHandler";
        asciidoctor.registerLogHandler(logRecord -> {
            throw new RuntimeException(errorMessage);
        });
        // when
        try {
            asciidoctor.convert(
                    "= Test\n\n== Something different\n\n[big]\nHello World",
                    Options.builder().option("sourcemap", "true").build());
        } catch (Throwable t) {
            // then
            assertThat(t.getMessage()).contains("Failed to load AsciiDoc document");
            final Throwable cause = t.getCause();
            assertThat(cause.getClass()).isEqualTo(RuntimeException.class);
            assertThat(cause.getMessage()).isEqualTo(errorMessage);
        }
    }
}

