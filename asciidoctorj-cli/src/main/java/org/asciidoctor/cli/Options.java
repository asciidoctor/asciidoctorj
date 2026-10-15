package org.asciidoctor.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.SafeMode;
import org.asciidoctor.converter.Converter;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Processor;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.jruby.converter.spi.ConverterRegistry;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;
import org.asciidoctor.jruby.syntaxhighlighter.spi.SyntaxHighlighterRegistry;
import org.asciidoctor.log.Severity;

/**
 * List of options that can be specified on the command line.
 *
 * <p>The options are stored in a Map (like a Hash in Ruby), using the following keys: attributes, input_files,
 * output_file, safe, standalone, template_dirs, template_engine, eruby, verbose, warnings, load_paths, requires,
 * base_dir, source_dir, destination_dir, log_level, failure_level, sourcemap, trace and timings.
 */
public class Options extends LinkedHashMap<String, Object> {
    static final String FS = "/";
    static final String RS = "\\";

    private static final List<String> LOG_LEVELS = List.of("debug", "DEBUG", "info", "INFO", "warning", "WARNING",
            "error", "ERROR", "fatal", "FATAL");

    private static final List<String> FAILURE_LEVELS = List.of("info", "INFO", "warning", "WARNING", "error", "ERROR",
            "fatal", "FATAL");

    // NOTE classes can't be loaded at runtime by a native executable (GraalVM)
    private static final boolean NATIVE_IMAGE = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    /**
     * The class loader used to load the required classes and the services (extended by the load paths and the
     * required JAR files).
     */
    private ClassLoader classLoader = Options.class.getClassLoader();
    // the extension registries loaded with the -r option, which are applied to the Asciidoctor instance
    private final List<ExtensionRegistry> extensionRegistries = new ArrayList<>();
    // the converter registries, syntax highlighter registries, converters and extensions loaded with the -r option
    private final List<ConverterRegistry> converterRegistries = new ArrayList<>();
    private final List<SyntaxHighlighterRegistry> syntaxHighlighterRegistries = new ArrayList<>();
    private final List<Class<?>> converterClasses = new ArrayList<>();
    private final List<Consumer<JavaExtensionRegistry>> processorRegistrations = new ArrayList<>();

    public Options(Map<String, Object> options) {
        if (options == null) {
            options = Map.of();
        }
        Object attributes = options.get("attributes");
        put("attributes", attributes == null ? new LinkedHashMap<String, Object>() : attributes);
        put("input_files", options.get("input_files"));
        put("output_file", options.get("output_file"));
        Object safe = options.get("safe");
        put("safe", safe == null ? SafeMode.UNSAFE.getLevel() : safe);
        put("standalone", options.getOrDefault("standalone", true));
        put("template_dirs", options.get("template_dirs"));
        put("template_engine", options.get("template_engine"));
        if (options.get("doctype") != null) {
            getAttributes().put("doctype", options.get("doctype"));
        }
        if (options.get("backend") != null) {
            getAttributes().put("backend", options.get("backend"));
        }
        put("eruby", options.get("eruby"));
        put("verbose", options.getOrDefault("verbose", 1));
        put("warnings", options.getOrDefault("warnings", false));
        put("load_paths", options.get("load_paths"));
        put("requires", options.get("requires"));
        put("base_dir", options.get("base_dir"));
        put("source_dir", options.get("source_dir"));
        put("destination_dir", options.get("destination_dir"));
        put("log_level", options.get("log_level"));
        put("failure_level", Severity.FATAL);
        put("sourcemap", options.get("sourcemap"));
        put("trace", false);
        put("timings", false);
    }

    public Options() {
        this(null);
    }

    /**
     * Parses the command line arguments.
     *
     * @return the parsed {@link Options} or the Integer exit code if the arguments could not be parsed or no
     *         conversion should be performed (e.g., when the help is requested)
     */
    public static Object parse(List<String> args) {
        return new Options().parseArgs(args);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAttributes() {
        return (Map<String, Object>) get("attributes");
    }

    /**
     * Parses the command line arguments into this object.
     *
     * @return this object or the Integer exit code if the arguments could not be parsed or no conversion should be
     *         performed (e.g., when the help is requested)
     */
    public Object parseArgs(List<String> args) {
        List<String> remaining = new ArrayList<>(args);
        OptionParser optsParser = createParser();
        try {
            Integer exitCode = optsParser.parse(remaining);
            if (exitCode != null) {
                return exitCode;
            }

            if (remaining.isEmpty()) {
                if (Integer.valueOf(2).equals(get("verbose"))) {
                    // -v flag was specified
                    return printVersion(System.out);
                }
                puts(System.err, optsParser);
                return 1;
            }

            List<String> infiles = new ArrayList<>();
            // shave off the file to process so that options errors appear correctly
            if (remaining.size() == 1 && remaining.get(0).equals("-")) {
                infiles.add(remaining.remove(remaining.size() - 1));
            } else {
                for (String file : remaining) {
                    if (file.startsWith("-")) {
                        // warn, but don't panic; we may have enough to proceed, so we won't force a failure
                        puts(System.err, "asciidoctor: WARNING: extra arguments detected (unparsed arguments: '"
                                + String.join("', '", remaining) + "') or incorrect usage of stdin");
                    } else if (isFile(file)) {
                        infiles.add(file);
                    // NOTE only attempt to glob if file is not found
                    } else {
                        // Tilt backslashes in Windows paths the Ruby-friendly way
                        if (File.separator.equals(RS) && file.contains(RS)) {
                            file = file.replace(RS, FS);
                        }
                        List<String> matches = glob(file);
                        if (matches.isEmpty()) {
                            // NOTE if no matches, assume it's just a missing file and proceed
                            infiles.add(file);
                        } else {
                            infiles.addAll(matches);
                        }
                    }
                }
            }

            for (String file : infiles) {
                if (file.equals("-")) {
                    continue;
                }
                Path path = toPath(file);
                if (path == null || !Files.exists(path)) {
                    puts(System.err, "asciidoctor: FAILED: input file " + file + " is missing");
                    return 1;
                } else if (Files.isDirectory(path)) {
                    puts(System.err, "asciidoctor: FAILED: input path " + file + " is a directory, not a file");
                    return 1;
                } else if (!Files.isReadable(path)) {
                    puts(System.err, "asciidoctor: FAILED: input file " + file + " is not readable");
                    return 1;
                }
            }

            put("input_files", infiles);

            if (getAttributes().isEmpty()) {
                remove("attributes");
            }

            // the class path is made of the load paths and of the required JAR files (whose services are loaded)
            List<String> classPath = new ArrayList<>();
            if (get("load_paths") instanceof List<?> loadPaths) {
                List<String> uniqueLoadPaths = new ArrayList<>(new LinkedHashSet<>(stringList(loadPaths)));
                put("load_paths", uniqueLoadPaths);
                classPath.addAll(uniqueLoadPaths);
            }
            List<String> uniqueRequires = List.of();
            if (get("requires") instanceof List<?> requires) {
                uniqueRequires = new ArrayList<>(new LinkedHashSet<>(stringList(requires)));
                put("requires", uniqueRequires);
                for (String path : uniqueRequires) {
                    if (isJar(path)) {
                        if (!isFile(path)) {
                            return failToRequire(path, new FileNotFoundException(path));
                        }
                        classPath.add(path);
                    }
                }
            }
            if (!classPath.isEmpty()) {
                if (NATIVE_IMAGE) {
                    puts(System.err, "asciidoctor: FAILED: the native executable can't load classes at runtime "
                            + "(-I DIRECTORY, -r FILE.jar)");
                    puts(System.err, "  Run AsciidoctorJ with Java to load extensions, converters and syntax "
                            + "highlighters: java -jar asciidoctorj-cli.jar");
                    return 1;
                }
                classLoader = createClassLoader(classPath);
            }
            for (String path : uniqueRequires) {
                // NOTE the services of a required JAR file are loaded by the Asciidoctor instance
                if (!isJar(path)) {
                    try {
                        require(path);
                    } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
                        return failToRequire(path, e);
                    }
                }
            }

            return this;
        } catch (OptionParser.MissingArgumentException e) {
            puts(System.err, "asciidoctor: option " + e.getMessage());
            puts(System.out, optsParser);
            return 1;
        } catch (OptionParser.ParseException e) {
            puts(System.err, "asciidoctor: " + e.getMessage());
            puts(System.out, optsParser);
            return 1;
        }
    }

    private OptionParser createParser() {
        OptionParser opts = new OptionParser("""
                Usage: asciidoctor [OPTION]... FILE...
                Convert the AsciiDoc input FILE(s) to the backend output format (e.g., HTML 5, DocBook 5, etc.)
                Unless specified otherwise, the output is written to a file whose name is derived from the input file.
                Application log messages are printed to STDERR.
                Example: asciidoctor input.adoc
                """);

        opts.on("-b", "--backend BACKEND", null,
                List.of("set backend output format: [html5, xhtml5, docbook5, manpage] (default: html5)",
                        "additional backends are supported via extended converters (e.g., pdf, epub3)"),
                backend -> getAttributes().put("backend", backend));
        opts.on("-d", "--doctype DOCTYPE", List.of("article", "book", "manpage", "inline"),
                List.of("document type to use when converting document: [article, book, manpage, inline] (default: article)"),
                doctype -> getAttributes().put("doctype", doctype));
        opts.on("-e", "--embedded", null,
                List.of("suppress enclosing document structure and output an embedded document (default: false)"),
                arg -> put("standalone", false));
        opts.on("-o", "--out-file FILE", null,
                List.of("output file (default: based on path of input file); use - to output to STDOUT"),
                outputFile -> put("output_file", outputFile));
        opts.on(null, "--safe", null,
                List.of("set safe mode level to safe (default: unsafe)",
                        "enables include directives, but prevents access to ancestor paths of source file",
                        "provided for compatibility with the asciidoc command"),
                arg -> put("safe", SafeMode.SAFE.getLevel()));
        List<String> safeModeNames = Arrays.stream(SafeMode.values()).map(mode -> mode.name().toLowerCase(Locale.ROOT)).toList();
        opts.on("-S", "--safe-mode SAFE_MODE", safeModeNames,
                List.of("set safe mode level explicitly: [" + String.join(", ", safeModeNames) + "] (default: unsafe)",
                        "disables potentially dangerous macros in source files, such as include::[]"),
                name -> put("safe", SafeMode.valueOf(name.toUpperCase(Locale.ROOT)).getLevel()));
        opts.on(null, "--sourcemap", null, List.of("add source location information to each parsed block (default: false)"),
                arg -> put("sourcemap", true));
        opts.on("-s", "--no-header-footer", null,
                List.of("suppress enclosing document structure and output an embedded document (default: false)"),
                arg -> put("standalone", false));
        opts.on("-n", "--section-numbers", null,
                List.of("auto-number section titles in the HTML backend; disabled by default"),
                arg -> getAttributes().put("sectnums", ""));
        opts.on(null, "--eruby ERUBY", List.of("erb", "erubi", "erubis"),
                List.of("specify eRuby implementation to use when rendering custom ERB templates: [erb, erubi, erubis] (default: erb)"),
                eruby -> put("eruby", eruby));
        opts.on("-a", "--attribute name[=value]", null,
                List.of("a document attribute to set in the form of name, name!, or name=value pair",
                        "that takes precedence over the same attribute defined in the source document",
                        "unless either the name or value ends in @ (i.e., name@=value or name=value@)",
                        "may be specified more than once"),
                attr -> {
                    if ((attr = attr.stripTrailing()).isEmpty() || attr.equals("=")) {
                        return;
                    }
                    int idx = attr.indexOf('=');
                    getAttributes().put(idx < 0 ? attr : attr.substring(0, idx), idx < 0 ? "" : attr.substring(idx + 1));
                });
        opts.on("-T", "--template-dir DIR", null,
                List.of("a directory containing custom converter templates that override the built-in converter (requires tilt gem)",
                        "may be specified more than once"),
                this::addTemplateDir);
        opts.on("-E", "--template-engine NAME", null,
                List.of("template engine to use for the custom converter templates (loads gem on demand)"),
                templateEngine -> put("template_engine", templateEngine));
        opts.on("-B", "--base-dir DIR", null,
                List.of("base directory containing the document and resources (default: directory of source file)"),
                baseDir -> put("base_dir", baseDir));
        opts.on("-R", "--source-dir DIR", null,
                List.of("source root directory (used for calculating path in destination directory)"),
                srcDir -> put("source_dir", srcDir));
        opts.on("-D", "--destination-dir DIR", null,
                List.of("destination output directory (default: directory of source file)"),
                destDir -> put("destination_dir", destDir));
        opts.on("-I", "--load-path DIRECTORY", null,
                List.of("add a directory (or JAR file) to the class path used to load the required classes",
                        "may be specified more than once"),
                path -> appendAll("load_paths", Arrays.asList(path.split(File.pathSeparator))));
        opts.on("-r", "--require CLASS", null,
                List.of("load the specified class (an implementation of ExtensionRegistry, ConverterRegistry,",
                        "SyntaxHighlighterRegistry, Converter or of an extension, such as a BlockProcessor)",
                        "or JAR file (whose services are loaded)",
                        "may be specified more than once"),
                path -> appendAll("requires", List.of(path)));
        opts.on(null, "--log-level LEVEL", LOG_LEVELS,
                List.of("set minimum level of log messages that get logged: [DEBUG, INFO, WARN, ERROR, FATAL] (default: WARN)"),
                level -> put("log_level", severityFor(level)));
        opts.on(null, "--failure-level LEVEL", FAILURE_LEVELS,
                List.of("set minimum log level that yields a non-zero exit code: [INFO, WARN, ERROR, FATAL] (default: FATAL)"),
                level -> put("failure_level", severityFor(level)));
        opts.on("-q", "--quiet", null, List.of("silence application log messages and script warnings (default: false)"),
                arg -> put("verbose", 0));
        opts.on(null, "--trace", null, List.of("include backtrace information when reporting errors (default: false)"),
                arg -> put("trace", true));
        opts.on("-v", "--verbose", null,
                List.of("show all application log messages, including DEBUG and INFO levels (default: false)"),
                arg -> put("verbose", 2));
        opts.on("-w", "--warnings", null, List.of("turn on script warnings (default: false)"),
                arg -> put("warnings", true));
        opts.on("-t", "--timings", null, List.of("print timings report (default: false)"),
                arg -> put("timings", true));
        opts.onTail("-h", "--help [TOPIC]",
                List.of("print a help message", "show this usage if TOPIC is not specified or recognized",
                        "show an overview of the AsciiDoc syntax if TOPIC is syntax",
                        "dump the Asciidoctor man page (in troff/groff format) if TOPIC is manpage"),
                topic -> printHelp(topic, opts));
        opts.onTail("-V", "--version",
                List.of("display the version and runtime environment (or -v if no other flags or arguments)"),
                arg -> printVersion(System.out));
        return opts;
    }

    private void addTemplateDir(String templateDir) {
        Object templateDirs = get("template_dirs");
        if (templateDirs == null) {
            put("template_dirs", new ArrayList<>(List.of(templateDir)));
        } else if (templateDirs instanceof List<?> list) {
            List<Object> dirs = new ArrayList<>(list);
            dirs.add(templateDir);
            put("template_dirs", dirs);
        } else {
            put("template_dirs", new ArrayList<>(List.of(templateDirs, templateDir)));
        }
    }

    private void appendAll(String key, List<String> values) {
        List<String> list = get(key) instanceof List<?> existing ? new ArrayList<>(stringList(existing))
                : new ArrayList<>();
        list.addAll(values);
        put(key, list);
    }

    private static List<String> stringList(List<?> values) {
        return values.stream().map(String::valueOf).toList();
    }

    private static Severity severityFor(String level) {
        level = level.toUpperCase(Locale.ROOT);
        return Severity.valueOf(level.equals("WARNING") ? "WARN" : level);
    }

    private static List<String> glob(String pattern) {
        if (!pattern.contains("*") && !pattern.contains("?") && !pattern.contains("[") && !pattern.contains("{")) {
            return List.of();
        }
        // the base directory is the part of the pattern that precedes the first segment that contains a wildcard
        String[] segments = pattern.split(FS, -1);
        StringBuilder base = new StringBuilder();
        int idx = 0;
        while (idx < segments.length - 1 && !segments[idx].matches(".*[*?\\[{].*")) {
            base.append(segments[idx]).append(FS);
            idx++;
        }
        Path baseDir = Path.of(base.isEmpty() ? "." : base.toString());
        if (!Files.isDirectory(baseDir)) {
            return List.of();
        }
        PathMatcher matcher = baseDir.getFileSystem().getPathMatcher("glob:" + pattern);
        boolean relativeToCwd = base.isEmpty();
        try (Stream<Path> paths = Files.walk(baseDir)) {
            return paths.map(path -> relativeToCwd ? baseDir.relativize(path) : path)
                    .filter(path -> matcher.matches(path))
                    .map(path -> path.toString().replace(File.separator, FS))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private static ClassLoader createClassLoader(List<String> loadPaths) {
        List<URL> urls = new ArrayList<>();
        for (String path : loadPaths) {
            try {
                urls.add(Path.of(path).toAbsolutePath().toUri().toURL());
            } catch (MalformedURLException e) {
                // ignore invalid path
            }
        }
        return new URLClassLoader(urls.toArray(URL[]::new), Options.class.getClassLoader());
    }

    private int failToRequire(String path, Throwable cause) {
        closeClassLoader();
        if (Boolean.TRUE.equals(get("trace"))) {
            throw new IllegalStateException("cannot load such class -- " + path, cause);
        }
        puts(System.err, "asciidoctor: FAILED: '" + path + "' could not be loaded");
        if (NATIVE_IMAGE) {
            puts(System.err, "  The native executable can only load the classes it contains; "
                    + "run AsciidoctorJ with Java to load other classes");
        }
        puts(System.err, "  Use --trace to show backtrace");
        return 1;
    }

    private static boolean isJar(String path) {
        return path.toLowerCase(Locale.ROOT).endsWith(".jar");
    }

    /**
     * Loads the class with the specified name. If the class is a registry (the service provider interfaces used to
     * register extensions, converters and syntax highlighters automatically), a converter or an extension (a processor),
     * it will be registered with the Asciidoctor instance.
     */
    private void require(String className) throws ReflectiveOperationException {
        Class<?> clazz = Class.forName(className, true, classLoader);
        if (ExtensionRegistry.class.isAssignableFrom(clazz)) {
            extensionRegistries.add((ExtensionRegistry) clazz.getConstructor().newInstance());
        } else if (ConverterRegistry.class.isAssignableFrom(clazz)) {
            converterRegistries.add((ConverterRegistry) clazz.getConstructor().newInstance());
        } else if (SyntaxHighlighterRegistry.class.isAssignableFrom(clazz)) {
            syntaxHighlighterRegistries.add((SyntaxHighlighterRegistry) clazz.getConstructor().newInstance());
        } else if (Converter.class.isAssignableFrom(clazz)) {
            converterClasses.add(clazz);
        } else if (Processor.class.isAssignableFrom(clazz)) {
            Consumer<JavaExtensionRegistry> registration = processorRegistration(clazz);
            if (registration == null) {
                throw new ClassCastException("unsupported extension type: " + clazz.getName());
            }
            processorRegistrations.add(registration);
        }
    }

    /**
     * @return the function that registers the extension (a processor) with the extension registry, according to its
     * type, or null if the type isn't supported
     */
    @SuppressWarnings("unchecked")
    private static Consumer<JavaExtensionRegistry> processorRegistration(Class<?> clazz) {
        if (Preprocessor.class.isAssignableFrom(clazz)) {
            return registry -> registry.preprocessor((Class<? extends Preprocessor>) clazz);
        } else if (Treeprocessor.class.isAssignableFrom(clazz)) {
            return registry -> registry.treeprocessor((Class<? extends Treeprocessor>) clazz);
        } else if (Postprocessor.class.isAssignableFrom(clazz)) {
            return registry -> registry.postprocessor((Class<? extends Postprocessor>) clazz);
        } else if (IncludeProcessor.class.isAssignableFrom(clazz)) {
            return registry -> registry.includeProcessor((Class<? extends IncludeProcessor>) clazz);
        } else if (DocinfoProcessor.class.isAssignableFrom(clazz)) {
            return registry -> registry.docinfoProcessor((Class<? extends DocinfoProcessor>) clazz);
        } else if (BlockProcessor.class.isAssignableFrom(clazz)) {
            return registry -> registry.block((Class<? extends BlockProcessor>) clazz);
        } else if (BlockMacroProcessor.class.isAssignableFrom(clazz)) {
            return registry -> registry.blockMacro((Class<? extends BlockMacroProcessor>) clazz);
        } else if (InlineMacroProcessor.class.isAssignableFrom(clazz)) {
            return registry -> registry.inlineMacro((Class<? extends InlineMacroProcessor>) clazz);
        }
        return null;
    }

    /**
     * @return the extension registries loaded with the -r option
     */
    public List<ExtensionRegistry> getExtensionRegistries() {
        return extensionRegistries;
    }

    /**
     * @return the class loader used to load the required classes and the services, which includes the load paths
     * (-I) and the required JAR files (-r)
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Closes the class loader created for the load paths (-I) and the required JAR files (-r), if any, which releases
     * the JAR files (they can't be deleted on Windows while they are open).
     */
    public void closeClassLoader() {
        if (classLoader instanceof URLClassLoader urlClassLoader) {
            try {
                urlClassLoader.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Registers the registries, the converters and the extensions loaded with the -r option with the Asciidoctor instance.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerRequired(Asciidoctor asciidoctor) {
        extensionRegistries.forEach(registry -> registry.register(asciidoctor));
        converterRegistries.forEach(registry -> registry.register(asciidoctor));
        syntaxHighlighterRegistries.forEach(registry -> registry.register(asciidoctor));
        for (Class<?> converterClass : converterClasses) {
            asciidoctor.javaConverterRegistry().register((Class) converterClass);
        }
        processorRegistrations.forEach(registration -> registration.accept(asciidoctor.javaExtensionRegistry()));
    }

    private static boolean isFile(String path) {
        Path file = toPath(path);
        return file != null && Files.isRegularFile(file);
    }

    private static Path toPath(String path) {
        try {
            return Path.of(path);
        } catch (java.nio.file.InvalidPathException e) {
            return null;
        }
    }

    private static Integer printHelp(String topic, OptionParser opts) {
        switch (topic == null ? "" : topic) {
            // use `asciidoctor -h manpage | man -l -` to view with man pager
            case "manpage" -> {
                String manpagePath = System.getenv("ASCIIDOCTOR_MANPAGE_PATH");
                if (manpagePath != null) {
                    if (Files.exists(Path.of(manpagePath))) {
                        puts(System.out, readFile(Path.of(manpagePath)));
                    } else {
                        puts(System.err, "asciidoctor: FAILED: manual page not found: " + manpagePath);
                        return 1;
                    }
                } else {
                    String manpage = readResource("/org/asciidoctor/man/asciidoctor.1");
                    if (manpage == null) {
                        puts(System.err, "asciidoctor: FAILED: manual page not found; try `man asciidoctor`");
                        return 1;
                    }
                    puts(System.out, manpage);
                }
            }
            case "syntax" -> {
                String syntax = readResource("/org/asciidoctor/data/reference/syntax.adoc");
                if (syntax == null) {
                    puts(System.err, "asciidoctor: FAILED: syntax page not found; visit https://asciidoctor.org/docs");
                    return 1;
                }
                puts(System.out, syntax);
            }
            default -> puts(System.out, opts);
        }
        return 0;
    }

    private static String readFile(Path path) {
        try (InputStream in = Files.newInputStream(path);
                InputStream data = path.toString().endsWith(".gz") ? new GZIPInputStream(in) : in) {
            return new String(data.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String readResource(String path) {
        try (InputStream in = Options.class.getResourceAsStream(path)) {
            return in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Prints the version and the runtime environment.
     *
     * @return the exit code (0)
     */
    public int printVersion(PrintStream os) {
        String version;
        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
            version = asciidoctor.asciidoctorVersion();
        }
        puts(os, "AsciidoctorJ " + version + " [https://asciidoctor.org]");
        Map<String, String> encodings = new LinkedHashMap<>();
        encodings.put("lc", System.getProperty("native.encoding", Charset.defaultCharset().name()));
        encodings.put("fs", System.getProperty("sun.jnu.encoding", Charset.defaultCharset().name()));
        encodings.put("in", StandardCharsets.UTF_8.name());
        encodings.put("ex", Charset.defaultCharset().name());
        List<String> encodingInfo = encodings.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).toList();
        puts(os, "Runtime Environment (Java " + System.getProperty("java.version") + " "
                + System.getProperty("java.vendor") + ") (" + String.join(" ", encodingInfo) + ")");
        return 0;
    }

    /**
     * A minimal command line option parser modeled after Ruby's OptionParser.
     *
     * <p>Supports short options (which can be grouped and can have an attached argument), long options (which can be
     * abbreviated and can have an argument separated by an equals sign), required and optional arguments, arguments
     * restricted to a list of values (which can be abbreviated) and the -- terminator. Arguments that are not options
     * are retained in the list of arguments (in order).
     */
    static final class OptionParser {
        private static final int SUMMARY_WIDTH = 32;
        private static final String SUMMARY_INDENT = "    ";

        private final String banner;
        private final List<Option> options = new ArrayList<>();
        private final List<Option> tailOptions = new ArrayList<>();

        /**
         * A command line option.
         *
         * @param shortName the short form (e.g., -b) or null
         * @param longName the long form (e.g., --backend) or null
         * @param argName the name of the argument (e.g., BACKEND), or null if the option has no argument
         * @param argOptional whether the argument is optional
         * @param values the accepted values of the argument, or null to accept any value
         * @param description the lines of the description
         * @param handler the function that handles the option; returns an exit code to stop parsing, otherwise null
         */
        record Option(String shortName, String longName, String argName, boolean argOptional, List<String> values,
                List<String> description, java.util.function.Function<String, Integer> handler) {
        }

        static class ParseException extends RuntimeException {
            ParseException(String message) {
                super(message);
            }
        }

        static final class InvalidOptionException extends ParseException {
            InvalidOptionException(String arg) {
                super("invalid option: " + arg);
            }
        }

        static final class InvalidArgumentException extends ParseException {
            InvalidArgumentException(String args) {
                super("invalid argument: " + args);
            }
        }

        static final class MissingArgumentException extends ParseException {
            MissingArgumentException(String arg) {
                super("missing argument: " + arg);
            }
        }

        static final class AmbiguousOptionException extends ParseException {
            AmbiguousOptionException(String arg) {
                super("ambiguous option: " + arg);
            }
        }

        OptionParser(String banner) {
            this.banner = banner;
        }

        void on(String shortName, String longSpec, List<String> values, List<String> description,
                Consumer<String> handler) {
            options.add(option(shortName, longSpec, values, description, arg -> {
                handler.accept(arg);
                return null;
            }));
        }

        void onTail(String shortName, String longSpec, List<String> description,
                java.util.function.Function<String, Integer> handler) {
            tailOptions.add(option(shortName, longSpec, null, description, handler));
        }

        private static Option option(String shortName, String longSpec, List<String> values, List<String> description,
                java.util.function.Function<String, Integer> handler) {
            int space = longSpec.indexOf(' ');
            String longName = space < 0 ? longSpec : longSpec.substring(0, space);
            String argName = space < 0 ? null : longSpec.substring(space + 1);
            boolean argOptional = argName != null && argName.startsWith("[") && argName.endsWith("]")
                    && !argName.contains("=");
            return new Option(shortName, longName, argName, argOptional, values, description, handler);
        }

        private Stream<Option> allOptions() {
            return Stream.concat(options.stream(), tailOptions.stream());
        }

        /**
         * Parses the options in the arguments, removing them from the list (so only the non-option arguments remain).
         *
         * @return the exit code returned by an option handler (which stops the parsing), otherwise null
         */
        Integer parse(List<String> args) {
            List<String> remaining = new ArrayList<>();
            for (int i = 0; i < args.size(); i++) {
                String arg = args.get(i);
                if (arg.equals("--")) {
                    remaining.addAll(args.subList(i + 1, args.size()));
                    break;
                } else if (arg.startsWith("--")) {
                    int eq = arg.indexOf('=');
                    String name = eq < 0 ? arg : arg.substring(0, eq);
                    Option option = findLong(name, arg);
                    String value = eq < 0 ? null : arg.substring(eq + 1);
                    if (option.argName() == null) {
                        if (value != null) {
                            throw new ParseException("needless argument: " + arg);
                        }
                    } else if (value == null) {
                        if (i + 1 < args.size() && !(option.argOptional() && args.get(i + 1).startsWith("-"))) {
                            value = args.get(++i);
                        } else if (!option.argOptional()) {
                            throw new MissingArgumentException(name);
                        }
                    }
                    Integer exitCode = invoke(option, value, eq < 0 ? name + " " + value : arg);
                    if (exitCode != null) {
                        return exitCode;
                    }
                } else if (arg.startsWith("-") && arg.length() > 1) {
                    // a group of short options (e.g., -we)
                    for (int c = 1; c < arg.length(); c++) {
                        String name = "-" + arg.charAt(c);
                        String invalidOption = c == 1 ? arg : name;
                        Option option = allOptions().filter(o -> name.equals(o.shortName())).findFirst()
                                .orElseThrow(() -> new InvalidOptionException(invalidOption));
                        String value = null;
                        if (option.argName() != null) {
                            if (c + 1 < arg.length()) {
                                value = arg.substring(c + 1);
                            } else if (i + 1 < args.size() && !(option.argOptional() && args.get(i + 1).startsWith("-"))) {
                                value = args.get(++i);
                            } else if (!option.argOptional()) {
                                throw new MissingArgumentException(name);
                            }
                            c = arg.length();
                        }
                        Integer exitCode = invoke(option, value, name + " " + value);
                        if (exitCode != null) {
                            return exitCode;
                        }
                    }
                } else {
                    remaining.add(arg);
                }
            }
            args.clear();
            args.addAll(remaining);
            return null;
        }

        private Option findLong(String name, String arg) {
            List<Option> exact = allOptions().filter(o -> name.equals(o.longName())).toList();
            if (!exact.isEmpty()) {
                return exact.get(0);
            }
            // a long option can be abbreviated as long as the abbreviation is unique
            List<Option> candidates = allOptions().filter(o -> o.longName() != null && o.longName().startsWith(name))
                    .toList();
            if (candidates.size() == 1) {
                return candidates.get(0);
            } else if (candidates.isEmpty()) {
                throw new InvalidOptionException(arg);
            }
            throw new AmbiguousOptionException(name);
        }

        private static Integer invoke(Option option, String value, String argsForMessage) {
            if (value != null && option.values() != null) {
                value = complete(option.values(), value, argsForMessage);
            }
            return option.handler().apply(value);
        }

        /**
         * Resolves the value from the list of accepted values, allowing the value to be abbreviated.
         */
        private static String complete(List<String> values, String value, String argsForMessage) {
            if (values.contains(value)) {
                return value;
            }
            List<String> candidates = values.stream().filter(v -> v.startsWith(value)).toList();
            if (candidates.size() == 1) {
                return candidates.get(0);
            }
            throw new InvalidArgumentException(argsForMessage);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder(banner).append('\n');
            allOptions().forEach(option -> summarize(option, result));
            // remove the trailing newline (like OptionParser#to_s when printed with puts)
            result.setLength(result.length() - 1);
            return result.toString();
        }

        private static void summarize(Option option, StringBuilder result) {
            String spec = (option.shortName() == null ? "    " : option.shortName() + ", ") + option.longName()
                    + (option.argName() == null ? "" : " " + option.argName());
            String left = SUMMARY_INDENT + spec;
            List<String> description = option.description();
            if (left.length() > SUMMARY_INDENT.length() + SUMMARY_WIDTH) {
                result.append(left).append('\n');
                left = "";
            }
            String padding = " ".repeat(SUMMARY_INDENT.length() + SUMMARY_WIDTH + 1);
            for (int i = 0; i < description.size(); i++) {
                String prefix = i == 0 && !left.isEmpty() ? left + " ".repeat(padding.length() - left.length()) : padding;
                result.append(prefix).append(description.get(i)).append('\n');
            }
        }
    }

    /**
     * Prints the message followed by a line feed, independent of the platform (like puts in Ruby).
     */
    private static void puts(PrintStream out, Object message) {
        out.print(message + "\n");
        out.flush();
    }
}
