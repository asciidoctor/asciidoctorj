package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Port of logger_test.rb (ported by hand since it relies on Ruby-specific features such as mixins).
 */
@DisplayName("Logger")
public class LoggerTest extends TestHelper {

    static class MyLogger extends Logger {
        MyLogger(PrintStream out) {
            super(out);
        }

        MyLogger() {
            super();
        }
    }

    @Nested
    @DisplayName("LoggerManager")
    class LoggerManagerTests {

        @Test
        @DisplayName("provides access to logger via static logger method")
        void providesAccessToLoggerViaStaticLoggerMethod() throws Exception {
            var logger = LoggerManager.getLogger();
            assertNotNull(logger);
            assertInstanceOf(Logger.class, logger);
        }

        @Test
        @DisplayName("allows logger instance to be changed")
        void allowsLoggerInstanceToBeChanged() throws Exception {
            var oldLogger = LoggerManager.getLogger();
            var newLogger = new MyLogger(System.out);
            try {
                LoggerManager.setLogger(newLogger);
                assertSame(newLogger, LoggerManager.getLogger());
            } finally {
                LoggerManager.setLogger(oldLogger);
            }
        }

        @Test
        @DisplayName("setting logger instance to falsy value resets instance to default logger")
        void settingLoggerInstanceToFalsyValueResetsInstanceToDefaultLogger() throws Exception {
            var oldLogger = LoggerManager.getLogger();
            try {
                LoggerManager.setLogger(new MyLogger(System.out));
                LoggerManager.setLogger(null);
                assertNotNull(LoggerManager.getLogger());
                assertInstanceOf(Logger.class, LoggerManager.getLogger());
            } finally {
                LoggerManager.setLogger(oldLogger);
            }
        }

        @Test
        @DisplayName("creates logger instance from static logger_class property")
        void createsLoggerInstanceFromStaticLoggerClassProperty() throws Exception {
            Supplier<Logger> oldLoggerFactory = LoggerManager.getLoggerFactory();
            var oldLogger = LoggerManager.getLogger();
            try {
                LoggerManager.setLoggerFactory(MyLogger::new);
                LoggerManager.setLogger(null);
                assertNotNull(LoggerManager.getLogger());
                assertInstanceOf(MyLogger.class, LoggerManager.getLogger());
            } finally {
                LoggerManager.setLoggerFactory(oldLoggerFactory);
                LoggerManager.setLogger(oldLogger);
            }
        }
    }

    @Nested
    @DisplayName("Logger")
    class LoggerTests {

        @Test
        @DisplayName("should set logdev to $stderr by default")
        void shouldSetLogdevToStderrByDefault() throws Exception {
            String outString;
            String errString;
            try (var streams = redirectStreams()) {
                var logger = new Logger();
                logger.warn("this is a call");
                outString = streams.out().getString();
                errString = streams.err().getString();
            }
            assertEmpty(outString);
            refuteEmpty(errString);
            assertIncludes(errString, "this is a call");
        }

        @Test
        @DisplayName("should set level to value specified by level kwarg")
        void shouldSetLevelToValueSpecifiedByLevelKwarg() throws Exception {
            String outString;
            String errString;
            Logger.Severity logLevel;
            try (var streams = redirectStreams()) {
                var logger = new Logger(System.err, Logger.Severity.FATAL);
                logger.warn("this is a call");
                outString = streams.out().getString();
                errString = streams.err().getString();
                logLevel = logger.getLevel();
            }
            assertEmpty(outString);
            assertEmpty(errString);
            assertEquals(Logger.Severity.FATAL, logLevel);
        }

        @Test
        @DisplayName("should set logdev to specified file")
        void shouldSetLogdevToSpecifiedFile(@TempDir Path tmpDir) throws Exception {
            var logFile = tmpDir.resolve("asciidoctor.log");
            try (var out = new PrintStream(Files.newOutputStream(logFile), true)) {
                var logger = new Logger(out);
                logger.warn("this is a call");
            }
            var lines = Files.readAllLines(logFile);
            assertEquals("asciidoctor: WARNING: this is a call", lines.get(lines.size() - 1));
        }

        @Test
        @DisplayName("should set logdev to specified file with additional options")
        void shouldSetLogdevToSpecifiedFileWithAdditionalOptions(@TempDir Path tmpDir) throws Exception {
            var logFile = tmpDir.resolve("asciidoctor.log");
            try (var out = new PrintStream(Files.newOutputStream(logFile), true)) {
                // NOTE there's no formatter option in Java, so only the level is specified
                var logger = new Logger(out, Logger.Severity.DEBUG);
                logger.debug("this is a sign of life");
            }
            var lines = Files.readAllLines(logFile);
            assertEquals("asciidoctor: DEBUG: this is a sign of life", lines.get(lines.size() - 1));
        }

        @Test
        @DisplayName("should configure logger with progname set to asciidoctor")
        void shouldConfigureLoggerWithPrognameSetToAsciidoctor() throws Exception {
            assertEquals("asciidoctor", new Logger().getProgname());
        }

        @Test
        @DisplayName("should configure logger with level set to WARN by default")
        void shouldConfigureLoggerWithLevelSetToWARNByDefault() throws Exception {
            assertEquals(Logger.Severity.WARN, new Logger().getLevel());
        }

        @Test
        @DisplayName("configures default logger with progname set to asciidoctor")
        void configuresDefaultLoggerWithPrognameSetToAsciidoctor() throws Exception {
            assertEquals("asciidoctor", LoggerManager.getLogger().getProgname());
        }

        @Test
        @DisplayName("configures default logger with level set to WARN")
        void configuresDefaultLoggerWithLevelSetToWARN() throws Exception {
            assertEquals(Logger.Severity.WARN, LoggerManager.getLogger().getLevel());
        }

        @Test
        @DisplayName("configures default logger to write messages to $stderr")
        void configuresDefaultLoggerToWriteMessagesToStderr() throws Exception {
            String outString;
            String errString;
            try (var streams = redirectStreams()) {
                LoggerManager.getLogger().warn("this is a call");
                outString = streams.out().getString();
                errString = streams.err().getString();
            }
            assertEmpty(outString);
            refuteEmpty(errString);
            assertIncludes(errString, "this is a call");
        }

        @Test
        @DisplayName("configures default logger to use a formatter that matches traditional format")
        void configuresDefaultLoggerToUseAFormatterThatMatchesTraditionalFormat() throws Exception {
            String errString;
            try (var streams = redirectStreams()) {
                LoggerManager.getLogger().warn("this is a call");
                LoggerManager.getLogger().fatal("it cannot be done");
                errString = streams.err().getString();
            }
            assertIncludes(errString, "asciidoctor: WARNING: this is a call");
            assertIncludes(errString, "asciidoctor: FAILED: it cannot be done");
        }

        @Test
        @DisplayName("NullLogger level is not nil")
        void nullLoggerLevelIsNotNil() throws Exception {
            var logger = new NullLogger();
            assertNotNull(logger.getLevel());
            assertEquals(Logger.Severity.UNKNOWN, logger.getLevel());
        }

        @Test
        @DisplayName("MemoryLogger level is not nil")
        void memoryLoggerLevelIsNotNil() throws Exception {
            var logger = new MemoryLogger();
            assertNotNull(logger.getLevel());
            assertEquals(Logger.Severity.UNKNOWN, logger.getLevel());
        }
    }

    // NOTE unlike Ruby, the logger API option doesn't replace the global logger; it's bound to the current scope
    @Nested
    @DisplayName(":logger API option")
    class LoggerAPIOption {
        static final String INPUT = """
                ....
                unterminated literal block
                """;

        Logger oldLogger;
        MemoryLogger globalLogger;

        @BeforeEach
        void replaceGlobalLogger() {
            oldLogger = LoggerManager.getLogger();
            LoggerManager.setLogger(globalLogger = new MemoryLogger());
        }

        @AfterEach
        void restoreGlobalLogger() {
            LoggerManager.setLogger(oldLogger);
        }

        void assertLoggedTo(MemoryLogger logger) {
            assertEquals(List.of("unterminated literal block"),
                    logger.getMessages().stream().map(MemoryLogger.Message::text).toList());
            assertTrue(globalLogger.isEmpty());
            assertSame(globalLogger, LoggerManager.getLogger());
        }

        Path writeInputFile(Path dir) throws Exception {
            return Files.writeString(dir.resolve("input.adoc"), INPUT);
        }

        @Test
        @DisplayName("should be able to set logger when invoking load API")
        void shouldBeAbleToSetLoggerWhenInvokingLoadAPI() throws Exception {
            var newLogger = new MemoryLogger();
            Asciidoctor.load(INPUT, Options.of("logger", newLogger));
            assertLoggedTo(newLogger);
        }

        @Test
        @DisplayName("should be able to set logger when invoking load_file API")
        void shouldBeAbleToSetLoggerWhenInvokingLoad_fileAPI(@TempDir Path dir) throws Exception {
            var newLogger = new MemoryLogger();
            Asciidoctor.loadFile(writeInputFile(dir), Options.of("logger", newLogger));
            assertLoggedTo(newLogger);
        }

        @Test
        @DisplayName("should be able to set logger when invoking convert API")
        void shouldBeAbleToSetLoggerWhenInvokingConvertAPI() throws Exception {
            var newLogger = new MemoryLogger();
            Asciidoctor.convert(INPUT, Options.of("logger", newLogger));
            assertLoggedTo(newLogger);
        }

        @Test
        @DisplayName("should be able to set logger when invoking convert_file API")
        void shouldBeAbleToSetLoggerWhenInvokingConvert_fileAPI(@TempDir Path dir) throws Exception {
            var newLogger = new MemoryLogger();
            Asciidoctor.convertFile(writeInputFile(dir), Options.of("to_file", false, "logger", newLogger));
            assertLoggedTo(newLogger);
        }

        @Test
        @DisplayName("should use logger when document loaded with :logger option is parsed later")
        void shouldUseLoggerWhenDocumentLoadedWithLoggerOptionIsParsedLater() throws Exception {
            var newLogger = new MemoryLogger();
            var doc = Asciidoctor.load(INPUT, Options.of("logger", newLogger, "parse", false));
            assertTrue(newLogger.isEmpty());
            doc.convert();
            assertLoggedTo(newLogger);
        }

        @Test
        @DisplayName("should be able to disable logging by setting :logger option to a falsy value")
        void shouldBeAbleToDisableLoggingBySettingLoggerOptionToAFalsyValue() throws Exception {
            for (var falsyVal : new Object[] { null, false }) {
                Asciidoctor.convert(INPUT, Options.of("logger", falsyVal));
                assertTrue(globalLogger.isEmpty());
                assertSame(globalLogger, LoggerManager.getLogger());
            }
        }

        @Test
        @DisplayName("should not share logger between concurrent conversions")
        void shouldNotShareLoggerBetweenConcurrentConversions() throws Exception {
            var loggers = new ArrayList<MemoryLogger>();
            var threads = new ArrayList<Thread>();
            for (var i = 0; i < 10; i++) {
                var logger = new MemoryLogger();
                // NOTE the line number of the warning identifies the conversion
                var input = "\n".repeat(i) + INPUT;
                loggers.add(logger);
                Thread thread = new Thread(() -> Asciidoctor.convert(input, Options.of("logger", logger)));
                thread.start();
                threads.add(thread);
            }
            for (var thread : threads) {
                thread.join();
            }
            for (var i = 0; i < loggers.size(); i++) {
                var messages = loggers.get(i).getMessages();
                assertEquals(1, messages.size());
                var logMessage = assertInstanceOf(LogMessage.class, messages.get(0).message());
                assertEquals(i + 1, logMessage.sourceLocation().getLineNumber());
            }
            assertTrue(globalLogger.isEmpty());
        }
    }

    // the Ruby Logging mixin is ported as an interface; static access is provided by Logging.logger()

    interface SampleModuleA extends Logging {
        default Logger retrieveLogger() {
            return getLogger();
        }
    }

    static class SampleClassA implements SampleModuleA {
    }

    interface SampleModuleB extends Logging {
        static Logger retrieveLogger() {
            return Logging.logger();
        }
    }

    static class SampleClassC implements Logging {
        Logger retrieveLogger() {
            return getLogger();
        }
    }

    static class SampleClassD implements Logging {
        static Logger retrieveLogger() {
            return Logging.logger();
        }
    }

    static class SampleClassE implements Logging {
        LogMessage createMessage(Reader.Cursor cursor) {
            return Logging.messageWithContext("Asciidoctor was here", cursor);
        }
    }

    @Nested
    @DisplayName("Logging")
    class LoggingTests {

        @Test
        @DisplayName("including Logging gives instance methods on module access to logging infrastructure")
        void includingLoggingGivesInstanceMethodsOnModuleAccessToLoggingInfrastructure() throws Exception {
            assertSame(LoggerManager.getLogger(), new SampleClassA().retrieveLogger());
            assertNotNull(SampleClassA.class.getMethod("getLogger"));
        }

        @Test
        @DisplayName("including Logging gives static methods on module access to logging infrastructure")
        void includingLoggingGivesStaticMethodsOnModuleAccessToLoggingInfrastructure() throws Exception {
            assertSame(LoggerManager.getLogger(), SampleModuleB.retrieveLogger());
        }

        @Test
        @DisplayName("including Logging gives instance methods on class access to logging infrastructure")
        void includingLoggingGivesInstanceMethodsOnClassAccessToLoggingInfrastructure() throws Exception {
            assertSame(LoggerManager.getLogger(), new SampleClassC().retrieveLogger());
            assertNotNull(SampleClassC.class.getMethod("getLogger"));
        }

        @Test
        @DisplayName("including Logging gives static methods on class access to logging infrastructure")
        void includingLoggingGivesStaticMethodsOnClassAccessToLoggingInfrastructure() throws Exception {
            assertSame(LoggerManager.getLogger(), SampleClassD.retrieveLogger());
        }

        @Test
        @DisplayName("can create an auto-formatting message with context")
        void canCreateAnAutoFormattingMessageWithContext() throws Exception {
            var cursor = new Reader.Cursor("file.adoc", fixturedir(), "file.adoc", 5);
            var message = new SampleClassE().createMessage(cursor);
            assertEquals("Asciidoctor was here", message.text());
            assertSame(cursor, message.sourceLocation());
            assertEquals("file.adoc: line 5: Asciidoctor was here", message.toString());
        }

        @Test
        @DisplayName("writes message prefixed with program name and source location to stderr")
        void writesMessagePrefixedWithProgramNameAndSourceLocationToStderr() throws Exception {
            var input = """
                    [#first]
                    first paragraph

                    [#first]
                    another first paragraph
                    """;
            String messages;
            try (var streams = redirectStreams()) {
                convertStringToEmbedded(input);
                messages = Strings.chomp(streams.err().getString());
            }
            assertEquals("asciidoctor: WARNING: <stdin>: line 5: id assigned to block already in use: first", messages);
        }
    }
}
