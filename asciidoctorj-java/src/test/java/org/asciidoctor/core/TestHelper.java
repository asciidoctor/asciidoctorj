package org.asciidoctor.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;

import org.asciidoctor.core.MemoryLogger.Message;
import org.asciidoctor.core.Reader.Cursor;

/**
 * The base class of the tests, which provides the helpers of the Ruby test suite (test_helper.rb) as well as helpers
 * that mirror the Ruby core methods used by the translated tests.
 */
public abstract class TestHelper {
    private static final Pattern RE_XMLNS_ATTRIBUTE = Pattern.compile(" xmlns=\"[^\"]+\"");

    /** A Runnable that may throw a checked exception. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** A Consumer that may throw a checked exception. */
    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T value) throws Exception;
    }

    /** A BiConsumer that may throw a checked exception. */
    @FunctionalInterface
    public interface ThrowingBiConsumer<T, U> {
        void accept(T first, U second) throws Exception;
    }

    /** A captured output stream (mirrors StringIO). */
    public static final class CapturedStream {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final PrintStream stream = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        public PrintStream getStream() {
            return stream;
        }

        public String getString() {
            return buffer.toString(StandardCharsets.UTF_8);
        }

        @Override
        public String toString() {
            return getString();
        }
    }

    // --- environment ---

    /**
     * Invokes the Java accessor that corresponds to the Ruby method name on the target (mirrors Object#send as used
     * with a block pass, e.g., map(&:text)).
     */
    @SuppressWarnings("unchecked")
    public static <T> T send(Object target, String rubyMethod) {
        if (target instanceof Map<?, ?> map) {
            return (T) map.get(rubyMethod);
        }
        // NOTE List#getFirst and List#getLast are only available as of Java 21
        if (target instanceof List<?> && ("first".equals(rubyMethod) || "last".equals(rubyMethod))) {
            return "first".equals(rubyMethod) ? first(target) : last(target);
        }
        // the Java names that differ from the Ruby names (to match the API of AsciidoctorJ)
        rubyMethod = switch (rubyMethod) {
            case "lineno" -> "line_number";
            case "sectname" -> "section_name";
            case "reftext?" -> "is_reftext";
            default -> rubyMethod;
        };
        boolean predicate = rubyMethod.endsWith("?");
        String name = Strings.camelCase(predicate ? rubyMethod.substring(0, rubyMethod.length() - 1) : rubyMethod);
        String capitalized = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        List<String> candidates = predicate ? List.of("is" + capitalized, "has" + capitalized, name)
                : List.of("get" + capitalized, name, "is" + capitalized);
        for (String candidate : candidates) {
            try {
                var method = target.getClass().getMethod(candidate);
                return (T) method.invoke(target);
            } catch (NoSuchMethodException e) {
                // try the next candidate
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("No method " + rubyMethod + " on " + target.getClass());
    }

    /**
     * Fails the test with the specified message (mirrors Minitest's flunk).
     */
    public static void flunk(String message) {
        fail(message);
    }

    /**
     * Asserts the content is well-formed XML (without loading external entities).
     */
    public static void assertWellFormedXml(String content) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        var builder = factory.newDocumentBuilder();
        builder.setErrorHandler(null);
        builder.parse(new InputSource(new StringReader(content)));
    }

    /**
     * @return the attribute entries stored in the specified attributes (i.e., attrs[:attribute_entries])
     */
    @SuppressWarnings("unchecked")
    public static List<Document.AttributeEntry> attributeEntries(Map<String, Object> attributes) {
        return (List<Document.AttributeEntry>) attributes.get(Document.ATTRIBUTE_ENTRIES_KEY);
    }

    /**
     * @return the sum of the numeric values (mirrors Enumerable#sum and reduce(:+))
     */
    public static Number sum(Object values) {
        long longSum = 0;
        double doubleSum = 0;
        boolean decimal = false;
        for (Object value : (Iterable<?>) iterable(values)) {
            Number number = value instanceof Number n ? n : Double.valueOf(String.valueOf(value));
            if (number instanceof Double || number instanceof Float) {
                decimal = true;
            }
            longSum += number.longValue();
            doubleSum += number.doubleValue();
        }
        // NOTE a whole number is returned as an Integer since 100 == 100.0 in Ruby
        return decimal && doubleSum != Math.rint(doubleSum) ? (Number) doubleSum : (Number) (int) Math.round(doubleSum);
    }

    /**
     * Asserts the object responds to the Ruby method. For a converter, a convert_<name> method is resolved as a
     * handler for the <name> node.
     */
    public static void assertRespondTo(Object target, String rubyMethod) {
        if (target instanceof org.asciidoctor.core.converter.AbstractConverter converter && rubyMethod.startsWith("convert_")) {
            assertTrue(converter.handles(rubyMethod.substring(8)), "Expected converter to handle " + rubyMethod.substring(8));
            return;
        }
        String name = Strings.camelCase(rubyMethod.replace("?", ""));
        boolean found = java.util.Arrays.stream(target.getClass().getMethods())
                .anyMatch(m -> m.getName().equalsIgnoreCase(name) || m.getName().equalsIgnoreCase("get" + name)
                        || m.getName().equalsIgnoreCase("is" + name));
        assertTrue(found, "Expected " + target.getClass().getName() + " to respond to " + rubyMethod);
    }

    /**
     * Converts a filter that returns a Ruby-like verdict (e.g., true, false, nil, :prune, :reject, :skip or :stop)
     * into a filter that can be passed to {@link AbstractBlock#findBy(AbstractBlock.Selector, Function)}.
     */
    public static Function<AbstractBlock, AbstractBlock.FilterVerdict> filter(Function<AbstractBlock, Object> fn) {
        return node -> {
            Object verdict = fn.apply(node);
            if (verdict == null) {
                return AbstractBlock.FilterVerdict.SKIP;
            } else if (verdict instanceof Boolean b) {
                return b ? AbstractBlock.FilterVerdict.ACCEPT : AbstractBlock.FilterVerdict.SKIP;
            } else if (verdict instanceof AbstractBlock.FilterVerdict filterVerdict) {
                return filterVerdict;
            }
            return AbstractBlock.FilterVerdict.valueOf(verdict.toString().toUpperCase(Locale.ROOT));
        };
    }

    // --- CLI ---

    /**
     * @return the description of the runtime environment (the equivalent of RUBY_DESCRIPTION)
     */
    public static String runtimeDescription() {
        return "Java " + System.getProperty("java.version") + " " + System.getProperty("java.vendor");
    }

    /**
     * @return the alternate path separator (i.e., the backslash on Windows), or null (mirrors File::ALT_SEPARATOR)
     */
    public static String altSeparator() {
        return java.io.File.separatorChar == '\\' ? "\\" : null;
    }

    /**
     * The output of a command (both the standard output and the standard error streams).
     */
    public record CommandOutput(String text) {
        public String read() {
            return text;
        }

        public List<String> readLines() {
            return Helpers.eachLine(text);
        }
    }

    /**
     * Runs the command (mirrors run_command in the Ruby test helper).
     *
     * <p>The arguments are the command (a List), followed by the String arguments, optionally followed by a Map of
     * options (env: a Map of environment variables, where a null value removes the variable).
     *
     * @param block the function that processes the {@link CommandOutput}, or null
     *
     * @return the value returned by the function, or the output if no function is specified
     */
    @SuppressWarnings("unchecked")
    public static <T> T runCommand(List<?> args, Function<CommandOutput, T> block)
            throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        Map<String, Object> opts = Map.of();
        for (Object arg : args) {
            if (arg instanceof List<?> list) {
                list.forEach(item -> command.add(String.valueOf(item)));
            } else if (arg instanceof Map<?, ?> map) {
                opts = (Map<String, Object>) map;
            } else {
                command.add(String.valueOf(arg));
            }
        }
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        if (opts.get("env") instanceof Map<?, ?> env) {
            env.forEach((key, value) -> {
                if (value == null) {
                    builder.environment().remove(String.valueOf(key));
                } else {
                    builder.environment().put(String.valueOf(key), String.valueOf(value));
                }
            });
        }
        Process process = builder.start();
        process.getOutputStream().close();
        // NOTE like Ruby when reading in text mode, convert the Windows line endings
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");
        process.waitFor();
        CommandOutput result = new CommandOutput(output);
        return block == null ? (T) result : block.apply(result);
    }

    public static CommandOutput runCommand(List<?> args) throws IOException, InterruptedException {
        return runCommand(args, null);
    }

    /**
     * Formats the attributes like a Ruby Hash sorted by key (e.g., {1=>"value", "key"=>"val"}), where the positional
     * attributes (which have an Integer key in Ruby) are shown without quotes.
     */
    public static String inspectAttributes(Map<String, Object> attributes) {
        return attributes.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(e -> (e.getKey().matches("\\d+") ? e.getKey() : Strings.inspect(e.getKey())) + "=>"
                        + (e.getValue() instanceof String str ? Strings.inspect(str) : String.valueOf(e.getValue())))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * @return the value if it's truthy (not null or false), otherwise the default value (mirrors value || default)
     */
    @SuppressWarnings("unchecked")
    public static <T> T orElse(Object value, T defaultValue) {
        return isTruthy(value) ? (T) value : defaultValue;
    }

    /**
     * A mutable reference to a value, used for a local variable that is assigned inside a lambda.
     */
    public static final class Ref<T> {
        private T value;

        public Ref(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * Formats the arguments using the format (mirrors Kernel#sprintf).
     */
    public static String sprintf(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }

    public static boolean isJruby() {
        return false;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    public static String diskRoot() {
        return isWindows() ? Strings.partition(Helpers.workingDirectory(), "/")[0] + "/" : "/";
    }

    /**
     * @return the absolute path of the test directory (which contains the fixtures directory)
     */
    public static String testdir() {
        return Path.of("src/test/resources").toAbsolutePath().normalize().toString().replace('\\', '/');
    }

    public static String fixturedir() {
        return Helpers.joinPath(testdir(), "fixtures");
    }

    public static String fixturePath(String name) {
        return Helpers.joinPath(fixturedir(), name);
    }

    public static String sampleDocPath(String name) {
        if (!name.contains(".")) {
            for (String ext : List.of("adoc", "asciidoc", "txt")) {
                if (Files.exists(Path.of(fixturePath(name + "." + ext)))) {
                    name = name + "." + ext;
                    break;
                }
            }
        }
        return fixturePath(name);
    }

    // --- documents ---

    public static Document emptyDocument(Map<String, Object> options) {
        Map<String, Object> opts = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);
        Document doc = new Document(new ArrayList<String>(), opts);
        return Boolean.TRUE.equals(opts.get("parse")) ? doc.parse() : doc;
    }

    public static Document emptyDocument() {
        return emptyDocument(null);
    }

    public static Document emptySafeDocument(Map<String, Object> options) {
        Map<String, Object> opts = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);
        opts.put("safe", "safe");
        return new Document(new ArrayList<String>(), opts);
    }

    public static Document emptySafeDocument() {
        return emptySafeDocument(null);
    }

    public static Document exampleDocument(String name, Map<String, Object> opts) {
        return documentFromString(readFile(sampleDocPath(name)), opts);
    }

    public static Document exampleDocument(String name) {
        return exampleDocument(name, null);
    }

    public static Document documentFromString(String src, Map<String, Object> options) {
        Map<String, Object> opts = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);
        assignDefaultTestOptions(opts);
        boolean parse = Boolean.TRUE.equals(opts.get("parse"));
        Document doc = new Document(Helpers.eachLine(src), opts);
        return parse ? doc.parse() : doc;
    }

    public static Document documentFromString(String src) {
        return documentFromString(src, null);
    }

    public static AbstractBlock blockFromString(String src, Map<String, Object> options) {
        Map<String, Object> opts = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);
        opts.put("standalone", false);
        List<AbstractBlock> blocks = documentFromString(src, opts).getBlocks();
        return blocks.isEmpty() ? null : blocks.get(0);
    }

    public static AbstractBlock blockFromString(String src) {
        return blockFromString(src, null);
    }

    public static String convertString(String src, Map<String, Object> options) {
        Map<String, Object> opts = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);
        boolean keepNamespaces = Boolean.TRUE.equals(opts.remove("keep_namespaces"));
        String result = documentFromString(src, opts).convert();
        if (keepNamespaces || result == null) {
            return result;
        }
        // this is required because the XML parser is easily confused by namespaces
        return RE_XMLNS_ATTRIBUTE.matcher(result).replaceFirst("");
    }

    public static String convertString(String src) {
        return convertString(src, null);
    }

    public static String convertStringToEmbedded(String src, Map<String, Object> options) {
        Map<String, Object> opts = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);
        opts.put("standalone", false);
        return documentFromString(src, opts).convert();
    }

    public static String convertStringToEmbedded(String src) {
        return convertStringToEmbedded(src, null);
    }

    public static String convertInlineString(String src, Map<String, Object> options) {
        Map<String, Object> opts = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);
        opts.put("doctype", "inline");
        return documentFromString(src, opts).convert();
    }

    public static String convertInlineString(String src) {
        return convertInlineString(src, null);
    }

    public static Map<String, Object> parseHeaderMetadata(String source, Document doc) {
        Reader reader = new Reader(Strings.split(source, Asciidoctor.LF));
        return Parser.parseHeaderMetadata(reader, doc);
    }

    public static Map<String, Object> parseHeaderMetadata(String source) {
        return parseHeaderMetadata(source, null);
    }

    @SuppressWarnings("unchecked")
    public static void assignDefaultTestOptions(Map<String, Object> opts) {
        if (!opts.containsKey("standalone")) {
            opts.put("standalone", true);
        }
        if (!opts.containsKey("parse")) {
            opts.put("parse", true);
        }
        if (Boolean.TRUE.equals(opts.get("standalone"))) {
            // don't embed stylesheet unless test requests the default behavior
            if (opts.containsKey("linkcss_default")) {
                opts.remove("linkcss_default");
            } else {
                Object attrs = opts.get("attributes");
                Map<String, Object> attributes;
                if (attrs == null) {
                    attributes = new LinkedHashMap<>();
                } else if (attrs instanceof Map<?, ?> map) {
                    attributes = new LinkedHashMap<>((Map<String, Object>) map);
                } else {
                    attributes = Options.resolveAttributes(attrs);
                }
                attributes.put("linkcss", "");
                opts.put("attributes", attributes);
            }
        }
    }

    // --- XML assertions ---

    public static XmlDocument xmldocFromString(String content) {
        return XmlDocument.parse(content);
    }

    public static Object xmlnodesAtPath(String type, String path, Object content, Integer count) {
        XmlDocument doc = xmldocFromString((String) content);
        Object results = type.equals("xpath") ? doc.xpath(path) : doc.css(path);
        if (count != null && count == 1 && results instanceof List<?> list) {
            return list.isEmpty() ? null : list.get(0);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    public static XmlNodeSet xmlnodesAtXpath(String xpath, Object content) {
        return (XmlNodeSet) xmlnodesAtPath("xpath", xpath, content, null);
    }

    public static XmlNode xmlnodesAtXpath(String xpath, Object content, int count) {
        Object result = xmlnodesAtPath("xpath", xpath, content, count);
        if (result instanceof List<?> list) {
            return list.isEmpty() ? null : (XmlNode) list.get(0);
        }
        return (XmlNode) result;
    }

    @SuppressWarnings("unchecked")
    public static XmlNodeSet xmlnodesAtCss(String css, Object content) {
        return (XmlNodeSet) xmlnodesAtPath("css", css, content, null);
    }

    public static XmlNode xmlnodesAtCss(String css, Object content, int count) {
        Object result = xmlnodesAtPath("css", css, content, count);
        if (result instanceof List<?> list) {
            return list.isEmpty() ? null : (XmlNode) list.get(0);
        }
        return (XmlNode) result;
    }

    /**
     * Generate an xpath attribute matcher that matches a name in the class attribute.
     */
    public static String containsClass(String name) {
        return "contains(concat(' ', normalize-space(@class), ' '), ' " + name + " ')";
    }

    public static void assertCss(String css, Object content, Object count) {
        assertPath("css", css, content, count);
    }

    public static void assertCss(String css, Object content) {
        assertPath("css", css, content, null);
    }

    public static void assertXpath(String xpath, Object content, Object count) {
        assertPath("xpath", xpath, content, count);
    }

    public static void assertXpath(String xpath, Object content) {
        assertPath("xpath", xpath, content, null);
    }

    /**
     * Asserts the XPath or CSS selector matches the specified number of nodes in the content (a String, typically the
     * result of a conversion).
     */
    public static void assertPath(String type, String path, Object contentValue, Object count) {
        String typeName = type.equals("xpath") ? "XPath" : "CSS";
        assertNotNull(contentValue, typeName + " " + path + " cannot be evaluated against null content");
        String content = (String) contentValue;
        XmlDocument doc = xmldocFromString(content);
        Object results = type.equals("xpath") ? doc.xpath(path) : doc.css(path);
        if (count instanceof Boolean expected) {
            if (!expected.equals(results)) {
                fail(typeName + " " + path + " yielded " + results + " rather than " + count + " for:\n" + content);
            }
        } else if (count != null) {
            int size = results instanceof List<?> list ? list.size() : -1;
            if (results instanceof Double d) {
                size = d.intValue();
            }
            if (size != ((Number) count).intValue()) {
                fail(typeName + " " + path + " yielded " + size + " elements rather than " + count + " for:\n"
                        + content);
            }
        } else if (results instanceof List<?> list && list.isEmpty()) {
            fail(typeName + " " + path + " not found in:\n" + content);
        }
    }

    // --- logging ---

    public static void assertMessage(MemoryLogger logger, Object severity, String expectedMessage, Class<?> kind,
            Integer idx) {
        if (idx == null) {
            assertEquals(1, logger.getMessages().size(), () -> "expected 1 message, got: " + logger.getMessages());
            idx = 0;
        }
        Message message = logger.getMessages().get(idx);
        assertEquals(String.valueOf(severity), message.severity().name());
        assertInstanceOf(kind, message.message());
        String actualMessage;
        if (kind == String.class) {
            actualMessage = (String) message.message();
        } else {
            assertNotNull(((LogMessage) message.message()).sourceLocation());
            actualMessage = message.message().toString();
        }
        if (expectedMessage.startsWith("~")) {
            String expected = expectedMessage.substring(1);
            assertTrue(actualMessage.contains(expected), () -> "expected '" + actualMessage + "' to include '"
                    + expected + "'");
        } else {
            assertEquals(expectedMessage, actualMessage);
        }
    }

    public static void assertMessage(MemoryLogger logger, Object severity, String expectedMessage, Class<?> kind) {
        assertMessage(logger, severity, expectedMessage, kind, null);
    }

    public static void assertMessage(MemoryLogger logger, Object severity, String expectedMessage) {
        assertMessage(logger, severity, expectedMessage, String.class, null);
    }

    public static void assertMessages(MemoryLogger logger, List<?> expectedMessages) {
        assertEquals(expectedMessages.size(), logger.getMessages().size(), () -> "messages: " + logger.getMessages());
        for (int idx = 0; idx < expectedMessages.size(); idx++) {
            List<?> details = (List<?>) expectedMessages.get(idx);
            Class<?> kind = details.size() > 2 && details.get(2) != null ? (Class<?>) details.get(2) : String.class;
            assertMessage(logger, details.get(0), (String) details.get(1), kind, idx);
        }
    }

    /**
     * A memory logger that is installed as the logger of the processor until it's closed.
     */
    public static final class ScopedMemoryLogger extends MemoryLogger implements AutoCloseable {
        private final Logger oldLogger;

        ScopedMemoryLogger(Logger.Severity level) {
            oldLogger = LoggerManager.getLogger();
            if (level != null) {
                setLevel(level);
            }
            LoggerManager.setLogger(this);
        }

        @Override
        public void close() {
            LoggerManager.setLogger(oldLogger);
        }
    }

    public static ScopedMemoryLogger usingMemoryLogger(Logger.Severity level) {
        return new ScopedMemoryLogger(level);
    }

    public static ScopedMemoryLogger usingMemoryLogger() {
        return new ScopedMemoryLogger(null);
    }

    /**
     * Sets the level of the logger to DEBUG until it's closed.
     */
    public static final class VerboseMode implements AutoCloseable {
        private final Logger logger;
        private final Logger.Severity oldLevel;

        VerboseMode() {
            logger = LoggerManager.getLogger();
            oldLevel = logger.getLevel();
            logger.setLevel(Logger.Severity.DEBUG);
        }

        @Override
        public void close() {
            logger.setLevel(oldLevel);
        }
    }

    public static VerboseMode inVerboseMode() {
        return new VerboseMode();
    }

    /**
     * Redirects the standard output and error streams (and the logger) until it's closed.
     */
    public static final class RedirectedStreams implements AutoCloseable {
        private final PrintStream oldOut = System.out;
        private final PrintStream oldErr = System.err;
        private final Logger oldLogger = LoggerManager.getLogger();
        private final CapturedStream out = new CapturedStream();
        private final CapturedStream err = new CapturedStream();

        RedirectedStreams() {
            System.setOut(out.getStream());
            System.setErr(err.getStream());
            Logger newLogger = new Logger(err.getStream());
            newLogger.setLevel(oldLogger.getLevel());
            LoggerManager.setLogger(newLogger);
        }

        public CapturedStream out() {
            return out;
        }

        public CapturedStream err() {
            return err;
        }

        @Override
        public void close() {
            System.setOut(oldOut);
            System.setErr(oldErr);
            LoggerManager.setLogger(oldLogger);
        }
    }

    /**
     * Redirects the standard streams while the function is applied (to the captured output and error streams).
     *
     * @return the value returned by the function
     */
    public static <T> T redirectStreams(java.util.function.BiFunction<CapturedStream, CapturedStream, T> block) {
        try (var streams = redirectStreams()) {
            return block.apply(streams.out(), streams.err());
        }
    }

    public static <T> T redirectStreams(Function<CapturedStream, T> block) {
        try (var streams = redirectStreams()) {
            return block.apply(streams.out());
        }
    }

    public static RedirectedStreams redirectStreams() {
        return new RedirectedStreams();
    }

    // --- web server ---

    /** A Supplier that may throw a checked exception. */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    public static String resolveLocalhost() {
        return "127.0.0.1";
    }

    public static TestWebServer startTestWebserver() {
        return new TestWebServer(resolveLocalhost(), 9876, testdir());
    }

    public static <T> T usingTestWebserver(ThrowingSupplier<T> block) throws Exception {
        try (TestWebServer server = startTestWebserver()) {
            return block.get();
        }
    }

    // --- files ---

    public static String readFile(String path) {
        try {
            return Files.readString(Path.of(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean fileExists(String path) {
        Path file = Helpers.toPath(path);
        return file != null && Files.exists(file);
    }

    public static String joinPath(String... parts) {
        String result = parts[0];
        for (int i = 1; i < parts.length; i++) {
            result = Helpers.joinPath(result, parts[i]);
        }
        return result;
    }

    public static String fileBasename(String path) {
        return Helpers.basename(path);
    }

    public static String fileBasename(String path, String ext) {
        return Helpers.basename(path, ext);
    }

    public static String fileDirname(String path) {
        return Helpers.dirname(path);
    }

    public static String fileExtname(String path) {
        return Helpers.extname(path);
    }

    public static String fileAbsolutePath(String path) {
        return Helpers.expandPath(path, null);
    }

    public static String fileExpandPath(String path) {
        return Helpers.expandPath(path, null);
    }

    public static String fileExpandPath(String path, String base) {
        return Helpers.expandPath(path, base);
    }

    public static void assertPathExists(String path) {
        assertTrue(fileExists(path), () -> "expected path to exist: " + path);
    }

    public static void refutePathExists(String path) {
        assertTrue(!fileExists(path), () -> "expected path not to exist: " + path);
    }

    public static void withTmpdir(ThrowingConsumer<String> block) throws Exception {
        Path dir = Files.createTempDirectory("asciidoctor-test");
        try {
            block.accept(dir.toString().replace('\\', '/'));
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var children = Files.list(path)) {
                for (Path child : children.toList()) {
                    deleteRecursively(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    public static void copyFile(String source, String target) throws IOException {
        Files.copy(Path.of(source), Path.of(target), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * @return a copy of the List without the first n items (mirrors Array#drop)
     */
    public static <T> List<T> drop(List<T> items, int n) {
        return new ArrayList<>(items.subList(Math.min(n, items.size()), items.size()));
    }

    /**
     * Appends the items of the other Lists to the List (mirrors Array#concat).
     *
     * @return the List
     */
    @SafeVarargs
    public static <T> List<T> concat(List<T> items, List<? extends T>... others) {
        for (List<? extends T> other : others) {
            items.addAll(other);
        }
        return items;
    }

    /**
     * @return a List that contains the items repeated the specified number of times (mirrors Array#*)
     */
    public static <T> List<T> multiply(List<T> items, int times) {
        List<T> result = new ArrayList<>(items.size() * times);
        for (int i = 0; i < times; i++) {
            result.addAll(items);
        }
        return result;
    }

    /**
     * Removes the directory and its contents, if it exists (mirrors FileUtils.rm_rf).
     */
    public static void removeDir(Object path) {
        Path dir = Helpers.toPath(String.valueOf(path));
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (var paths = Files.walk(dir)) {
            for (Path p : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Creates the directory, including any missing parent directories (mirrors FileUtils.mkdir_p).
     */
    public static void makeDir(Object path) {
        try {
            Files.createDirectories(Path.of(String.valueOf(path)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void removeFile(String path) {
        try {
            Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // --- Ruby-isms ---

    public static String decodeChar(int number) {
        return new String(Character.toChars(number));
    }

    @SafeVarargs
    public static <T> List<T> list(T... items) {
        List<T> result = new ArrayList<>();
        Collections.addAll(result, items);
        return result;
    }

    public static String str(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof List<?> l) {
            return Strings.inspect(l);
        }
        return value.toString();
    }

    public static int size(Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof CharSequence s) {
            return s.length();
        } else if (value instanceof Collection<?> c) {
            return c.size();
        } else if (value instanceof Map<?, ?> m) {
            return m.size();
        } else if (value instanceof Object[] a) {
            return a.length;
        } else if (value instanceof MemoryLogger logger) {
            return logger.getMessages().size();
        }
        throw new IllegalArgumentException("Cannot compute size of " + value.getClass());
    }

    public static boolean isEmpty(Object value) {
        return value instanceof Reader reader ? reader.isEmpty() : size(value) == 0;
    }

    public static boolean includes(Object container, Object item) {
        if (container == null) {
            return false;
        } else if (container instanceof String s) {
            return s.contains(String.valueOf(item));
        } else if (container instanceof Collection<?> c) {
            return c.contains(item);
        } else if (container instanceof Map<?, ?> m) {
            return m.containsKey(item);
        }
        throw new IllegalArgumentException("Cannot check inclusion in " + container.getClass());
    }

    public static <T> T first(java.util.List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    public static <T> T last(java.util.List<T> list) {
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    /**
     * Removes and returns the last element of the list, or null if the list is empty (mirrors Array#pop).
     */
    public static <T> T pop(java.util.List<T> list) {
        return list.isEmpty() ? null : list.remove(list.size() - 1);
    }

    /**
     * Removes and returns the first element of the list, or null if the list is empty (mirrors Array#shift).
     */
    public static <T> T shift(java.util.List<T> list) {
        return list.isEmpty() ? null : list.remove(0);
    }

    public static <V> V get(Map<?, V> map, Object key) {
        return map.get(key instanceof Integer i ? String.valueOf(i) : key);
    }

    public static <T> T get(java.util.List<T> list, Integer i) {
        return get(list, i.intValue());
    }

    public static <T> T get(java.util.List<T> list, int i) {
        return i < 0 ? (list.size() + i >= 0 ? list.get(list.size() + i) : null) : (i < list.size() ? list.get(i) : null);
    }

    public static String get(XmlNode node, String name) {
        return node.get(name);
    }

    @SafeVarargs
    public static Map<String, String> smap(String... keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    public static <K, V> java.util.List<Map.Entry<K, V>> entries(Map<K, V> map) {
        return new ArrayList<>(map.entrySet());
    }

    /**
     * Converts a list of pairs (2-element lists) to a list of entries.
     */
    public static <T> java.util.List<Map.Entry<T, T>> entries(java.util.List<? extends java.util.List<T>> pairs) {
        java.util.List<Map.Entry<T, T>> result = new ArrayList<>();
        for (java.util.List<T> pair : pairs) {
            result.add(new java.util.AbstractMap.SimpleEntry<>(pair.get(0), pair.size() > 1 ? pair.get(1) : null));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> java.util.List<T> plus(Object first, Object second) {
        java.util.List<T> result = new ArrayList<>((Collection<T>) first);
        result.addAll((Collection<T>) second);
        return result;
    }

    public static java.util.List<String> range(String from, String to) {
        java.util.List<String> result = new ArrayList<>();
        String current = from;
        while (true) {
            result.add(current);
            if (current.equals(to) || current.length() > to.length()) {
                break;
            }
            current = Strings.succ(current);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T first(Object value) {
        if (value instanceof List<?> l) {
            return l.isEmpty() ? null : (T) l.get(0);
        } else if (value instanceof Collection<?> c) {
            return c.isEmpty() ? null : (T) c.iterator().next();
        } else if (value instanceof String s) {
            return (T) (s.isEmpty() ? null : s.substring(0, 1));
        }
        throw new IllegalArgumentException("Cannot get first of " + value);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> first(Object value, int n) {
        List<T> list = (List<T>) value;
        return new ArrayList<>(list.subList(0, Math.min(n, list.size())));
    }

    @SuppressWarnings("unchecked")
    public static <T> T last(Object value) {
        if (value instanceof List<?> l) {
            return l.isEmpty() ? null : (T) l.get(l.size() - 1);
        } else if (value instanceof String s) {
            return (T) (s.isEmpty() ? null : s.substring(s.length() - 1));
        }
        throw new IllegalArgumentException("Cannot get last of " + value);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> last(Object value, int n) {
        List<T> list = (List<T>) value;
        return new ArrayList<>(list.subList(Math.max(0, list.size() - n), list.size()));
    }

    /**
     * Retrieves the value for the key from a Map, the attribute from an XmlNode or the entry from a log message.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object container, Object key) {
        if (container == null) {
            return null;
        } else if (container instanceof Map<?, ?> m) {
            return (T) m.get(key instanceof Integer i ? String.valueOf(i) : key);
        } else if (container instanceof List<?> l) {
            int i = ((Number) key).intValue();
            return (T) (i < 0 ? (l.size() + i >= 0 ? l.get(l.size() + i) : null) : (i < l.size() ? l.get(i) : null));
        } else if (container instanceof XmlNode n) {
            return (T) n.get(String.valueOf(key));
        } else if (container instanceof TestWebServer server) {
            return (T) server.getRequests();
        } else if (container instanceof Record r && key instanceof String name) {
            // access a record component by its (Ruby) name
            String component = Strings.camelCase(name);
            try {
                return (T) r.getClass().getMethod(component).invoke(r);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("Cannot get " + name + " from " + r.getClass(), e);
            }
        } else if (container instanceof Record r && key instanceof Integer i) {
            // destructure a record like a Ruby Array (e.g., the result of a method that returns multiple values)
            try {
                return (T) r.getClass().getRecordComponents()[i].getAccessor().invoke(r);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        } else if (container instanceof Message m) {
            return (T) switch (String.valueOf(key)) {
                case "severity" -> m.severity().name();
                case "message" -> m.message();
                default -> null;
            };
        } else if (container instanceof LogMessage m) {
            return (T) switch (String.valueOf(key)) {
                case "text" -> m.text();
                case "source_location" -> m.sourceLocation();
                case "include_location" -> m.includeLocation();
                default -> null;
            };
        } else if (container instanceof String s) {
            if (key instanceof Number n) {
                int i = n.intValue();
                return (T) (i < s.length() ? s.substring(i, i + 1) : null);
            }
            return (T) (s.contains(String.valueOf(key)) ? String.valueOf(key) : null);
        }
        throw new IllegalArgumentException("Cannot get " + key + " from " + container.getClass());
    }

    public static String join(Object values) {
        return join(values, "");
    }

    public static String join(Object values, String separator) {
        return ((Collection<?>) values).stream().map(TestHelper::str).collect(Collectors.joining(separator));
    }

    public static List<String> split(Object str) {
        return Strings.splitOnWhitespace(str.toString());
    }

    public static List<String> split(Object str, Object separator) {
        if (separator instanceof Pattern pattern) {
            List<String> result = new ArrayList<>(List.of(pattern.split(str.toString())));
            return result;
        }
        return Strings.split(str.toString(), String.valueOf(separator));
    }

    public static List<String> split(Object str, Object separator, int limit) {
        return Strings.split(str.toString(), String.valueOf(separator), limit);
    }

    /**
     * Splits the string into lines, retaining the line terminators (mirrors String#lines).
     */
    public static List<String> lines(Object value) {
        if (value instanceof Block block) {
            return block.getLines();
        } else if (value instanceof Reader reader) {
            return reader.getLines();
        } else if (value instanceof Table.Cell cell) {
            return cell.getLines();
        }
        return Helpers.eachLine(String.valueOf(value));
    }

    // typed overloads so the type of the lambda parameter is inferred from the collection

    public static <T, R> List<R> map(Iterable<T> values, Function<? super T, R> mapper) {
        List<R> result = new ArrayList<>();
        values.forEach(value -> result.add(mapper.apply(value)));
        return result;
    }

    public static <T> List<T> select(Iterable<T> values, Predicate<? super T> predicate) {
        List<T> result = new ArrayList<>();
        values.forEach(value -> {
            if (predicate.test(value)) {
                result.add(value);
            }
        });
        return result;
    }

    public static <T> List<T> reject(Iterable<T> values, Predicate<? super T> predicate) {
        return select(values, value -> !predicate.test(value));
    }

    public static <T> T find(Iterable<T> values, Predicate<? super T> predicate) {
        for (T value : values) {
            if (predicate.test(value)) {
                return value;
            }
        }
        return null;
    }

    public static <T> int count(Iterable<T> values, Predicate<? super T> predicate) {
        return select(values, predicate).size();
    }

    public static <T> boolean any(Iterable<T> values, Predicate<? super T> predicate) {
        for (T value : values) {
            if (predicate.test(value)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean all(Iterable<T> values, Predicate<? super T> predicate) {
        for (T value : values) {
            if (!predicate.test(value)) {
                return false;
            }
        }
        return true;
    }

    // Map variants iterate over the entries (mirrors Hash#map, Hash#select, etc.)

    public static <K, V, R> List<R> map(Map<K, V> values, java.util.function.BiFunction<K, V, R> mapper) {
        return map(values.entrySet(), e -> mapper.apply(e.getKey(), e.getValue()));
    }

    /**
     * Passes each item and the object to the function and returns the object (mirrors Enumerable#each_with_object).
     */
    public static <T, O> O eachWithObject(Iterable<T> values, O object, java.util.function.BiConsumer<T, O> fn) {
        values.forEach(value -> fn.accept(value, object));
        return object;
    }

    public static <K, V, R> List<R> map(Map<K, V> values, Function<Map.Entry<K, V>, R> mapper) {
        return map(values.entrySet(), mapper);
    }

    public static <K, V> List<Map.Entry<K, V>> select(Map<K, V> values, Predicate<Map.Entry<K, V>> predicate) {
        return select(values.entrySet(), predicate);
    }

    public static <K, V> Map.Entry<K, V> find(Map<K, V> values, Predicate<Map.Entry<K, V>> predicate) {
        return find(values.entrySet(), predicate);
    }

    public static int count(Object values, Object item) {
        if (values instanceof String s) {
            return Strings.count(s, String.valueOf(item).charAt(0));
        }
        int count = 0;
        for (Object value : (Iterable<?>) iterable(values)) {
            if (Objects.equals(value, item)) {
                count++;
            }
        }
        return count;
    }

    private static Object iterable(Object values) {
        if (values instanceof Map<?, ?> m) {
            return m.entrySet();
        } else if (values instanceof Object[] a) {
            return List.of(a);
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    public static <T> T copy(T value) {
        if (value == null) {
            return null;
        } else if (value instanceof Map<?, ?> m) {
            return (T) new LinkedHashMap<>(m);
        } else if (value instanceof List<?> l) {
            return (T) new ArrayList<>(l);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> merge(Object base, Object... others) {
        Map<String, Object> result = new LinkedHashMap<>((Map<String, Object>) base);
        for (Object other : others) {
            result.putAll((Map<String, Object>) other);
        }
        return result;
    }

    /**
     * An inclusive range of integers (mirrors Ruby's Range). The bounds are retained so a range with a negative end
     * (e.g., 1..-1) can be used to slice a List or a String.
     */
    public static final class IntRange extends AbstractList<Integer> {
        private final int from;
        private final int to;

        IntRange(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public Integer get(int index) {
            Objects.checkIndex(index, size());
            return from + index;
        }

        @Override
        public int size() {
            return Math.max(0, to - from + 1);
        }
    }

    public static List<Integer> range(int from, int to) {
        return new IntRange(from, to);
    }

    // mirrors Array#slice and String#slice (with a start index and a length or with a range)

    public static <T> List<T> slice(List<T> list, int start, int length) {
        int from = start < 0 ? list.size() + start : start;
        return new ArrayList<>(list.subList(from, Math.min(list.size(), from + length)));
    }

    public static String slice(String str, int start, int length) {
        int from = start < 0 ? str.length() + start : start;
        return str.substring(from, Math.min(str.length(), from + length));
    }

    private static int rangeStart(List<Integer> range) {
        return range instanceof IntRange r ? r.from : range.get(0);
    }

    private static int rangeEnd(List<Integer> range) {
        return range instanceof IntRange r ? r.to : range.get(range.size() - 1);
    }

    public static <T> List<T> slice(List<T> list, List<Integer> range) {
        int from = rangeStart(range) < 0 ? list.size() + rangeStart(range) : rangeStart(range);
        int to = rangeEnd(range) < 0 ? list.size() + rangeEnd(range) : rangeEnd(range);
        return new ArrayList<>(list.subList(from, Math.max(from, Math.min(list.size(), to + 1))));
    }

    public static String slice(String str, List<Integer> range) {
        int from = rangeStart(range) < 0 ? str.length() + rangeStart(range) : rangeStart(range);
        int to = rangeEnd(range) < 0 ? str.length() + rangeEnd(range) : rangeEnd(range);
        return str.substring(from, Math.max(from, Math.min(str.length(), to + 1)));
    }

    @SuppressWarnings("unchecked")
    public static <T> T slice(Object value, Object start, Object length) {
        int from = ((Number) start).intValue();
        int len = ((Number) length).intValue();
        return (T) (value instanceof String s ? slice(s, from, len) : slice((List<Object>) value, from, len));
    }

    @SuppressWarnings("unchecked")
    public static <T> T slice(Object value, Object range) {
        List<Integer> indexes = (List<Integer>) range;
        return (T) (value instanceof String s ? slice(s, indexes) : slice((List<Object>) value, indexes));
    }

    public static Pattern rx(String regex) {
        return Rx.compile(regex);
    }

    /**
     * Compiles the regex with the Ruby options (m for multiline, which matches newlines with ., and i for ignore case).
     */
    public static Pattern rx(String regex, String options) {
        int flags = Pattern.MULTILINE | Pattern.UNIX_LINES | (options.contains("m") ? Pattern.DOTALL : 0)
                | (options.contains("i") ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0);
        return Pattern.compile(regex, flags);
    }

    public static boolean matches(Object value, Object pattern) {
        if (value instanceof Pattern p) {
            return p.matcher(String.valueOf(pattern)).find();
        }
        Pattern p = pattern instanceof Pattern pat ? pat : Pattern.compile(Pattern.quote(String.valueOf(pattern)));
        return value != null && p.matcher(String.valueOf(value)).find();
    }

    public static Matcher match(Object value, Object pattern) {
        Pattern p = pattern instanceof Pattern pat ? pat : Pattern.compile(Pattern.quote(String.valueOf(pattern)));
        Matcher m = p.matcher(String.valueOf(value));
        return m.find() ? m : null;
    }

    public static List<String> scan(Object value, Pattern pattern) {
        List<String> result = new ArrayList<>();
        Matcher m = pattern.matcher(String.valueOf(value));
        while (m.find()) {
            result.add(m.groupCount() > 0 ? m.group(1) : m.group());
        }
        return result;
    }

    /**
     * Expands the Ruby back references (e.g., \\1 or \\k&lt;name&gt;) in the replacement using the match.
     */
    private static String expandReplacement(Matcher m, String replacement) {
        StringBuilder result = new StringBuilder();
        for (int i = 0, len = replacement.length(); i < len; i++) {
            char c = replacement.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char next = replacement.charAt(i + 1);
                if (Character.isDigit(next)) {
                    String group = m.group(next - '0');
                    result.append(group == null ? "" : group);
                    i++;
                    continue;
                } else if (next == 'k' && i + 2 < len && replacement.charAt(i + 2) == '<') {
                    int end = replacement.indexOf('>', i + 3);
                    String group = m.group(replacement.substring(i + 3, end));
                    result.append(group == null ? "" : group);
                    i = end;
                    continue;
                } else if (next == '\\') {
                    result.append('\\');
                    i++;
                    continue;
                }
            }
            result.append(c);
        }
        return result.toString();
    }

    public static String gsub(Object value, Object pattern, String replacement) {
        if (pattern instanceof Pattern p) {
            return Rx.gsub(p, String.valueOf(value), m -> expandReplacement(m, replacement));
        }
        return String.valueOf(value).replace(String.valueOf(pattern), replacement);
    }

    public static String sub(Object value, Object pattern, String replacement) {
        if (pattern instanceof Pattern p) {
            return Rx.sub(p, String.valueOf(value), m -> expandReplacement(m, replacement));
        }
        String str = String.valueOf(value);
        int idx = str.indexOf(String.valueOf(pattern));
        return idx < 0 ? str : str.substring(0, idx) + replacement + str.substring(idx
                + String.valueOf(pattern).length());
    }

    public static int toInt(Object value) {
        return Strings.toInt(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(Object value) {
        if (value instanceof List<?> l) {
            return new ArrayList<>((List<T>) l);
        } else if (value instanceof Collection<?> c) {
            return new ArrayList<>((Collection<T>) c);
        } else if (value instanceof Map<?, ?> m) {
            return (List<T>) new ArrayList<>(m.entrySet());
        }
        throw new IllegalArgumentException("Cannot convert to list: " + value);
    }

    @SuppressWarnings("unchecked")
    public static <K> List<K> keys(Object map) {
        if (map instanceof Document.Catalog) {
            // the catalog is a Hash in Ruby; its keys are the names of the fields
            return (List<K>) java.util.Arrays.stream(Document.Catalog.class.getDeclaredFields())
                    .filter(f -> !java.lang.reflect.Modifier.isStatic(f.getModifiers())).map(f -> f.getName()).toList();
        }
        return new ArrayList<>(((Map<K, ?>) map).keySet());
    }

    @SuppressWarnings("unchecked")
    public static <V> List<V> values(Object map) {
        return new ArrayList<>(((Map<?, V>) map).values());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<T> sorted(Object values) {
        List list = new ArrayList((Collection) values);
        Collections.sort(list);
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> uniq(Object values) {
        return new ArrayList<>(new LinkedHashSet<>((Collection<T>) values));
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> compact(Object values) {
        List<T> result = new ArrayList<>((Collection<T>) values);
        result.removeIf(Objects::isNull);
        return result;
    }

    public static List<Object> flatten(Object values) {
        List<Object> result = new ArrayList<>();
        for (Object value : (Collection<?>) values) {
            if (value instanceof Collection<?> c) {
                result.addAll(flatten(c));
            } else {
                result.add(value);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> reversed(Object values) {
        List<T> result = new ArrayList<>((Collection<T>) values);
        Collections.reverse(result);
        return result;
    }

    public static boolean startsWith(Object value, String... prefixes) {
        return value != null && Strings.startsWithAny(value.toString(), prefixes);
    }

    public static boolean endsWith(Object value, String... suffixes) {
        return value != null && Strings.endsWithAny(value.toString(), suffixes);
    }

    public static <T> T tap(T value, ThrowingConsumer<T> block) throws Exception {
        block.accept(value);
        return value;
    }

    // --- Minitest assertions ---

    public static boolean isTruthy(Object value) {
        return value != null && !Boolean.FALSE.equals(value);
    }

    public static void assertTruthy(Object value) {
        assertTrue(isTruthy(value), () -> "expected " + value + " to be truthy");
    }

    public static void assertTruthy(Object value, String message) {
        assertTrue(isTruthy(value), message);
    }

    public static void assertFalsy(Object value) {
        assertTrue(!isTruthy(value), () -> "expected " + value + " to be falsy");
    }

    public static void assertFalsy(Object value, String message) {
        assertTrue(!isTruthy(value), message);
    }

    public static void assertIncludes(Object collection, Object item) {
        assertTrue(includes(collection, item), () -> "expected " + collection + " to include " + item);
    }

    public static void refuteIncludes(Object collection, Object item) {
        assertTrue(!includes(collection, item), () -> "expected " + collection + " to not include " + item);
    }

    public static void assertInclude(Object expected, Object actual) {
        assertIncludes(actual, expected);
    }

    public static void refuteInclude(Object notExpected, Object actual) {
        refuteIncludes(actual, notExpected);
    }

    public static void assertIncludes(Object collection, Object item, Object message) {
        assertTrue(includes(collection, item), String.valueOf(message));
    }

    public static void refuteIncludes(Object collection, Object item, Object message) {
        assertTrue(!includes(collection, item), String.valueOf(message));
    }

    public static void assertEmpty(Object value, Object message) {
        assertTrue(isEmpty(value), String.valueOf(message));
    }

    public static void refuteEmpty(Object value, Object message) {
        assertTrue(!isEmpty(value), String.valueOf(message));
    }

    public static void assertMatch(Object pattern, Object value, Object message) {
        assertTrue(matches(value, pattern), String.valueOf(message));
    }

    public static void refuteMatch(Object pattern, Object value, Object message) {
        assertTrue(!matches(value, pattern), String.valueOf(message));
    }

    public static void assertEmpty(Object value) {
        assertTrue(isEmpty(value), () -> "expected " + value + " to be empty");
    }

    public static void refuteEmpty(Object value) {
        assertTrue(!isEmpty(value), () -> "expected " + value + " to not be empty");
    }

    public static void assertMatch(Object pattern, Object value) {
        assertTrue(matches(value, pattern), () -> "expected " + value + " to match " + pattern);
    }

    public static void refuteMatch(Object pattern, Object value) {
        assertTrue(!matches(value, pattern), () -> "expected " + value + " to not match " + pattern);
    }

    public static Cursor cursor(String file, String dir, String path, int lineno) {
        return new Cursor(file, dir, path, lineno);
    }
}
