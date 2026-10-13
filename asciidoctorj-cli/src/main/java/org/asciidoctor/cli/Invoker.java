package org.asciidoctor.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import java.util.logging.Level;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.core.Timings;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

/**
 * Invocation class for starting Asciidoctor via the CLI.
 *
 * <p>The documents are converted with the AsciidoctorJ API ({@link Asciidoctor}), except the timings, which are
 * collected by the implementation (the API doesn't provide them).
 */
public class Invoker {
    // the name of the java.util.logging logger of the default log handler of AsciidoctorJ
    private static final String JUL_LOGGER_NAME = "asciidoctor";

    private final Options options;
    private final List<Document> documents = new ArrayList<>();
    private Appendable out;
    private Appendable err;
    private int code;

    /**
     * Creates an invoker for the specified options, which may be an {@link Options} object, a Map of options or the
     * command line arguments.
     */
    @SuppressWarnings("unchecked")
    public Invoker(List<?> options) {
        Object first = options.isEmpty() ? null : options.get(0);
        Object result;
        if (first instanceof Options opts) {
            result = opts;
        } else if (first instanceof Map<?, ?> map) {
            result = new Options((Map<String, Object>) map);
        } else {
            result = Options.parse(options.stream().map(String::valueOf).toList());
        }
        if (result instanceof Integer exitCode) {
            this.code = exitCode;
            this.options = null;
        } else {
            this.options = (Options) result;
        }
    }

    public Invoker(String... args) {
        this(Arrays.asList(args));
    }

    public Invoker(Options options) {
        this(List.of(options));
    }

    public Invoker(Map<String, Object> options) {
        this(List.of(options));
    }

    public static void main(String[] args) {
        Invoker invoker = new Invoker(args);
        invoker.invoke();
        System.exit(invoker.getCode());
    }

    public Options getOptions() {
        return options;
    }

    /**
     * @return the documents that were converted (or the converted output if the output was not written to a file)
     */
    public List<Document> getDocuments() {
        return documents;
    }

    public int getCode() {
        return code;
    }

    /**
     * Converts the input files (or the standard input).
     */
    public void invoke() {
        invoke(null);
    }

    /**
     * Converts the input files (or the standard input, which can be supplied by the stdin function, which is
     * particularly useful for tests).
     */
    public void invoke(Supplier<?> stdin) {
        if (options == null) {
            return;
        }
        Appendable errOut = err == null ? System.err : err;
        Map<String, Object> opts = new LinkedHashMap<>();
        List<String> infiles = new ArrayList<>();
        String outfile = null;
        String absSrcdirPosix = null;
        boolean nonPosixEnv = File.separator.equals(Options.RS);
        boolean showTimings = false;
        CliLogHandler logHandler = new CliLogHandler();
        // NOTE the messages are logged by the log handler of the CLI (in the format of Asciidoctor), so the default log
        // handler of AsciidoctorJ (java.util.logging) is silenced
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(JUL_LOGGER_NAME);
        Level julLevel = julLogger.getLevel();
        julLogger.setLevel(Level.OFF);
        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
            asciidoctor.registerLogHandler(logHandler);
            for (ExtensionRegistry registry : options.getExtensionRegistries()) {
                registry.register(asciidoctor);
            }
            for (Map.Entry<String, Object> entry : options.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                switch (key) {
                    case "input_files" -> infiles = val == null ? List.of()
                            : ((List<?>) val).stream().map(String::valueOf).toList();
                    case "output_file" -> outfile = (String) val;
                    case "source_dir" -> {
                        if (val != null) {
                            absSrcdirPosix = absolutePath(Path.of(val.toString()));
                            if (nonPosixEnv && absSrcdirPosix.contains(Options.RS)) {
                                absSrcdirPosix = absSrcdirPosix.replace(Options.RS, Options.FS);
                            }
                        }
                    }
                    case "destination_dir" -> {
                        if (val != null) {
                            opts.put("to_dir", val);
                        }
                    }
                    // NOTE processor will dup attributes internally
                    case "attributes" -> opts.put("attributes", val);
                    case "timings" -> showTimings = Boolean.TRUE.equals(val);
                    case "trace", "requires", "load_paths" -> {
                        // no assignment
                    }
                    case "verbose" -> {
                        if (Integer.valueOf(0).equals(val)) {
                            logHandler.quiet = true;
                        } else if (Integer.valueOf(2).equals(val)) {
                            logHandler.level = Severity.DEBUG;
                        }
                    }
                    default -> {
                        if (val != null) {
                            opts.put(key, val);
                        }
                    }
                }
            }

            Object loggerLevel = opts.remove("log_level");
            if (loggerLevel != null) {
                logHandler.level = (Severity) loggerLevel;
            }
            Severity failureLevel = (Severity) opts.remove("failure_level");

            boolean useStdin = false;
            if (infiles.size() == 1) {
                String infile0 = infiles.get(0);
                if (infile0.equals("-")) {
                    if (outfile == null) {
                        outfile = infile0;
                    }
                    useStdin = true;
                } else if (isPipe(Path.of(infile0)) && outfile == null) {
                    outfile = "-";
                }
            }

            Object tofile = null;
            if ("-".equals(outfile)) {
                tofile = out == null ? utf8Stdout() : out;
            } else if (outfile != null) {
                opts.put("mkdirs", true);
                tofile = outfile;
            } else {
                opts.put("mkdirs", true);
                // automatically calculate outfile based on infile
            }

            if (useStdin) {
                String input = readInput(stdin == null ? System.in : stdin.get());
                Map<String, Object> inputOpts = new LinkedHashMap<>(opts);
                inputOpts.put("to_file", tofile);
                if (showTimings) {
                    Timings timings = new Timings();
                    inputOpts.put("timings", timings);
                    documents.add(asciidoctor.convert(input, toOptions(inputOpts), Document.class));
                    timings.printReport(errOut, "-");
                } else {
                    documents.add(asciidoctor.convert(input, toOptions(inputOpts), Document.class));
                }
            } else {
                for (String infile : infiles) {
                    Map<String, Object> inputOpts = new LinkedHashMap<>(opts);
                    inputOpts.put("to_file", tofile);
                    if (absSrcdirPosix != null && inputOpts.containsKey("to_dir")) {
                        String absIndir = dirname(absolutePath(Path.of(infile)));
                        String absIndirPosix = nonPosixEnv && absIndir.contains(Options.RS)
                                ? absIndir.replace(Options.RS, Options.FS) : absIndir;
                        if (absIndirPosix.startsWith(absSrcdirPosix + "/")) {
                            inputOpts.put("to_dir", inputOpts.get("to_dir") + absIndir.substring(absSrcdirPosix.length()));
                        }
                    }
                    if (showTimings) {
                        Timings timings = new Timings();
                        inputOpts.put("timings", timings);
                        documents.add(asciidoctor.convertFile(new File(infile), toOptions(inputOpts), Document.class));
                        timings.printReport(errOut, infile);
                    } else {
                        documents.add(asciidoctor.convertFile(new File(infile), toOptions(inputOpts), Document.class));
                    }
                }
            }
            Severity maxSeverity = logHandler.maxSeverity;
            if (maxSeverity != null && maxSeverity.compareTo(failureLevel) >= 0) {
                code = 1;
            }
        } catch (RuntimeException e) {
            code = 1;
            if (Boolean.TRUE.equals(options.get("trace"))) {
                throw e;
            }
            // NOTE the API wraps the exception of the processor, which has the message of the failure
            Throwable cause = e.getCause() != null && e.getMessage() != null && e.getMessage().startsWith("asciidoctor: ")
                    ? e.getCause() : e;
            println(errOut, cause.getClass() == RuntimeException.class ? e.getMessage() + " (RuntimeException)"
                    : e.getMessage());
            println(errOut, "  Use --trace to show backtrace");
        } finally {
            julLogger.setLevel(julLevel);
        }
    }

    /**
     * @return the options of the API that correspond to the options of the processor (Ruby option names)
     */
    private static org.asciidoctor.Options toOptions(Map<String, Object> opts) {
        org.asciidoctor.Options options = org.asciidoctor.Options.builder().build();
        opts.forEach(options::setOption);
        return options;
    }

    /**
     * @return the content of the standard input (an InputStream) or of the supplied input
     */
    private static String readInput(Object input) {
        if (input instanceof java.io.InputStream in) {
            try {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return input == null ? null : input.toString();
    }

    /**
     * @return the absolute and normalized path, using forward slashes as separators
     */
    static String absolutePath(Path path) {
        return path.toAbsolutePath().normalize().toString().replace(Options.RS, Options.FS);
    }

    /**
     * @return the parent directory of the (posix) path
     */
    static String dirname(String path) {
        int index = path.lastIndexOf(Options.FS);
        return index < 0 ? "." : index == 0 ? Options.FS : path.substring(0, index);
    }

    /**
     * The log handler of the CLI, which writes the messages to the standard error stream in the format of Asciidoctor
     * (e.g., asciidoctor: WARNING: sample.adoc: line 3: section title out of sequence) and keeps track of the maximum
     * severity (to honor the failure level).
     */
    private static final class CliLogHandler implements LogHandler {
        private static final String PROGNAME = "asciidoctor";

        Severity level = Severity.WARN;
        boolean quiet;
        Severity maxSeverity;

        @Override
        public void log(LogRecord logRecord) {
            Severity severity = logRecord.getSeverity() == null ? Severity.UNKNOWN : logRecord.getSeverity();
            if (maxSeverity == null || severity.compareTo(maxSeverity) > 0) {
                maxSeverity = severity;
            }
            if (quiet || severity.compareTo(level) < 0) {
                return;
            }
            String message = logRecord.getCursor() == null ? logRecord.getMessage()
                    : logRecord.getCursor() + ": " + logRecord.getMessage();
            System.err.print(PROGNAME + ": " + label(severity) + ": " + message + "\n");
            System.err.flush();
        }

        private static String label(Severity severity) {
            switch (severity) {
                case WARN:
                    return "WARNING";
                case FATAL:
                    return "FAILED";
                default:
                    return severity.name();
            }
        }
    }

    /**
     * @return the standard output stream, encoded as UTF-8 (regardless of the encoding of the console)
     */
    private static PrintStream utf8Stdout() {
        // NOTE the UTF-8 bytes are written as is to System.out (which may have been redirected), whatever its encoding
        return new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    /**
     * @return whether the path is a named pipe (or another special file that is not a regular file or directory)
     */
    static boolean isPipe(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class).isOther();
        } catch (IOException e) {
            return false;
        }
    }

    private static void println(Appendable target, String message) {
        try {
            target.append(message).append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return the first document that was converted
     */
    public Document getDocument() {
        return documents.isEmpty() ? null : documents.get(0);
    }

    /**
     * Redirects the output (when written to STDOUT) and the error messages to the specified targets (e.g., a
     * StringBuilder).
     */
    public void redirectStreams(Appendable out, Appendable err) {
        this.out = out;
        this.err = err;
    }

    public void redirectStreams(Appendable out) {
        redirectStreams(out, null);
    }

    public String readOutput() {
        return out == null ? "" : out.toString();
    }

    public String readError() {
        return err == null ? "" : err.toString();
    }

    public void resetStreams() {
        out = null;
        err = null;
    }
}
