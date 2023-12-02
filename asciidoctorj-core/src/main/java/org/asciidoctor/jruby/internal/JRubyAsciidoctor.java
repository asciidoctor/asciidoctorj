package org.asciidoctor.jruby.internal;

import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.extension.ExtensionGroup;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.RubyExtensionRegistry;
import org.asciidoctor.jruby.AsciidoctorJRuby;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.asciidoctor.jruby.converter.internal.ConverterRegistryExecutor;
import org.asciidoctor.jruby.extension.internal.ExtensionRegistryExecutor;
import org.asciidoctor.jruby.log.internal.JULLogHandler;
import org.asciidoctor.jruby.log.internal.JavaLogger;
import org.asciidoctor.jruby.log.internal.LogHandlerRegistryExecutor;
import org.asciidoctor.jruby.syntaxhighlighter.internal.SyntaxHighlighterRegistryExecutor;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterRegistry;
import org.jruby.*;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class JRubyAsciidoctor implements AsciidoctorJRuby, LogHandler {

    private static final Logger logger = Logger.getLogger(JRubyAsciidoctor.class.getName());

    private static final String GEM_PATH = "GEM_PATH";

    protected RubyGemsPreloader rubyGemsPreloader;

    protected Ruby rubyRuntime;

    private RubyClass extensionGroupClass;

    private final List<LogHandler> logHandlers = new ArrayList<>();

    public JRubyAsciidoctor() {
        this(createRubyRuntime(Collections.singletonMap(GEM_PATH, null), new ArrayList<>(), null));
        processRegistrations(this);
    }

    private JRubyAsciidoctor(final Ruby rubyRuntime) {
        this.rubyRuntime = rubyRuntime;

        InputStream inputStream = getClass().getResourceAsStream("asciidoctorclass.rb");
        final String script = IOUtils.readFull(inputStream);
        this.rubyRuntime.evalScriptlet(script);

        this.rubyGemsPreloader = new RubyGemsPreloader(this.rubyRuntime);
        this.logHandlers.add(new JULLogHandler());
        RubyOutputStreamWrapper.createOutputStreamWrapperClass(this.rubyRuntime);
    }

    public static JRubyAsciidoctor create() {
        return create((String) null);
    }

    public static JRubyAsciidoctor create(String gemPath) {
        return processRegistrations(createJRubyAsciidoctorInstance(Collections.singletonMap(GEM_PATH, gemPath), new ArrayList<>(), null));
    }

    public static JRubyAsciidoctor create(List<String> loadPaths) {
        return processRegistrations(createJRubyAsciidoctorInstance(null, loadPaths, null));
    }

    public static JRubyAsciidoctor create(ClassLoader classloader) {
        return processRegistrations(createJRubyAsciidoctorInstance(null, new ArrayList<>(), classloader));
    }

    public static JRubyAsciidoctor create(ClassLoader classloader, String gemPath) {
        return processRegistrations(createJRubyAsciidoctorInstance(Collections.singletonMap(GEM_PATH, gemPath), new ArrayList<>(), classloader));
    }

    public static JRubyAsciidoctor create(List<String> loadPaths, String gemPath) {
        return processRegistrations(createJRubyAsciidoctorInstance(Collections.singletonMap(GEM_PATH, gemPath), loadPaths, null));
    }

    private static JRubyAsciidoctor processRegistrations(JRubyAsciidoctor asciidoctor) {
        registerExtensions(asciidoctor);
        registerConverters(asciidoctor);
        registerSyntaxHighlighters(asciidoctor);
        registerLogHandlers(asciidoctor);

        JavaLogger.install(asciidoctor.getRubyRuntime(), asciidoctor);

        return asciidoctor;
    }

    private static void registerConverters(AsciidoctorJRuby asciidoctor) {
        new ConverterRegistryExecutor(asciidoctor).registerAllConverters();
    }

    private static void registerExtensions(AsciidoctorJRuby asciidoctor) {
        new ExtensionRegistryExecutor(asciidoctor).registerAllExtensions();
    }

    private static void registerSyntaxHighlighters(AsciidoctorJRuby asciidoctor) {
        new SyntaxHighlighterRegistryExecutor(asciidoctor).registerAllSyntaxHighlighter();
    }

    private static void registerLogHandlers(AsciidoctorJRuby asciidoctor) {
        new LogHandlerRegistryExecutor(asciidoctor).registerAllLogHandlers();
    }

    private static JRubyAsciidoctor createJRubyAsciidoctorInstance(Map<String, String> environmentVars, List<String> loadPaths, ClassLoader classloader) {
        Ruby rubyRuntime = createRubyRuntime(environmentVars, loadPaths, classloader);
        return new JRubyAsciidoctor(rubyRuntime);
    }

    private static Ruby createRubyRuntime(Map<String, String> environmentVars, List<String> loadPaths, ClassLoader classloader) {
        Map<String, String> env = environmentVars != null ? new HashMap<>(environmentVars) : new HashMap<>();

        RubyInstanceConfig config = createOptimizedConfiguration();
        if (classloader != null) {
            config.setLoader(classloader);
        }
        injectEnvironmentVariables(config, env);

        return JavaEmbedUtils.initialize(loadPaths, config);
    }

    private static void injectEnvironmentVariables(RubyInstanceConfig config, Map<String, String> environmentVars) {
        EnvironmentInjector environmentInjector = new EnvironmentInjector(config);
        environmentInjector.inject(environmentVars);
    }

    private static RubyInstanceConfig createOptimizedConfiguration() {
        return new RubyInstanceConfig();
    }

    @Override
    public void close() {
        if (rubyRuntime != null) {
            rubyRuntime.tearDown();
        }
    }

    @Override
    public void registerLogHandler(final LogHandler logHandler) {
        if (!this.logHandlers.contains(logHandler)) {
            this.logHandlers.add(logHandler);
        }
    }

    @Override
    public void unregisterLogHandler(final LogHandler logHandler) {
        this.logHandlers.remove(logHandler);
    }

    public Ruby getRubyRuntime() {
        return rubyRuntime;
    }

    private List<String> convertAllFiles(Options options, final Iterable<File> asciidoctorFiles) {
        List<String> asciidoctorContent = new ArrayList<>();
        for (File asciidoctorFile : asciidoctorFiles) {
            String renderedFile = convertFile(asciidoctorFile, options);
            if (renderedFile != null) {
                asciidoctorContent.add(renderedFile);
            }
        }
        return asciidoctorContent;
    }

    @Override
    public void requireLibrary(String... library) {
        requireLibraries(Arrays.asList(library));
    }

    @Override
    public void requireLibraries(Collection<String> libraries) {
        if (libraries != null) {
            for (String library : libraries) {
                RubyUtils.requireLibrary(rubyRuntime, library);
            }
        }
    }

    @Override
    public JavaExtensionRegistry javaExtensionRegistry() {
        return new JavaExtensionRegistryImpl(this);
    }

    public RubyExtensionRegistry rubyExtensionRegistry() {
        return new RubyExtensionRegistryImpl(rubyRuntime);
    }

    @Override
    public JavaConverterRegistry javaConverterRegistry() {
        return new JavaConverterRegistryImpl(this);
    }

    @Override
    public SyntaxHighlighterRegistry syntaxHighlighterRegistry() {
        return new SyntaxHighlighterRegistryImpl(this);
    }

    @Override
    public void unregisterAllExtensions() {
        getExtensionsModule().callMethod("unregister_all");
    }

    @Override
    public void shutdown() {
        this.rubyRuntime.tearDown();
    }

    @Override
    public String asciidoctorVersion() {
        return RubyUtils.rubyToJava(rubyRuntime, getAsciidoctorModule().getConstant("VERSION"), String.class);
    }

    private RubyModule getExtensionsModule() {
        return getAsciidoctorModule()
                .defineOrGetModuleUnder("Extensions");
    }

    private RubyModule getAsciidoctorModule() {
        return rubyRuntime.getModule("Asciidoctor");
    }

    public <T> T convert(String content, Map<String, Object> options, Class<T> expectedResult) {

        this.rubyGemsPreloader.preloadRequiredLibraries(options);

        if (containsAttributeWithValue(options, Attributes.SOURCE_HIGHLIGHTER, "pygments")) {
            logger.fine("In order to use Pygments with Asciidoctor, you need to install Pygments (and Python, if you don't have it yet). Read https://docs.asciidoctor.org/asciidoctor/latest/syntax-highlighting/pygments.");
        }

        String currentDirectory = rubyRuntime.getCurrentDirectory();

        if (options.containsKey(Options.BASEDIR)) {
            rubyRuntime.setCurrentDirectory((String) options.get(Options.BASEDIR));
        }

        final Object toFileOption = options.get(Options.TO_FILE);
        if (toFileOption instanceof OutputStream) {
            options.put(Options.TO_FILE, RubyOutputStreamWrapper.wrap(getRubyRuntime(), (OutputStream) toFileOption));
        }

        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

        try {

            IRubyObject object = getAsciidoctorModule().callMethod("convert",
                    Optional.ofNullable(content).map(rubyRuntime::newString).orElse(null), rubyHash);
            return adaptReturn(object, expectedResult);
        } catch (RaiseException e) {
            logger.severe(e.getException().getClass().getCanonicalName());
            throw new AsciidoctorCoreException(e);
        } finally {
            // we restore current directory to its original value.
            rubyRuntime.setCurrentDirectory(currentDirectory);
        }

    }

    private boolean containsAttributeWithValue(Map<String, Object> options, String attributeName, String attributeValue) {
        return Optional.ofNullable((Map<String, String>) options.get(Options.ATTRIBUTES))
                .map(attributes -> attributes.get(attributeName))
                .map(value -> value.equals(attributeValue))
                .orElse(Boolean.FALSE);
    }

    @Override
    public String convert(String content, Options options) {
        return convert(content, options, String.class);
    }

    @Override
    public <T> T convert(String content, Options options, Class<T> expectedResult) {
        return convert(content, options.map(), expectedResult);
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, Options options) throws IOException {
        final String content = IOUtils.readFull(contentReader);
        final String renderedContent = convert(content, options);
        IOUtils.writeFull(rendererWriter, renderedContent);
    }

    // @Override
    public <T> T convertFile(File file, Map<String, Object> options, Class<T> expectedResult) {

        this.rubyGemsPreloader.preloadRequiredLibraries(options);

        String currentDirectory = rubyRuntime.getCurrentDirectory();


        final Object toFileOption = options.get(Options.TO_FILE);
        if (toFileOption instanceof OutputStream) {
            options.put(Options.TO_FILE, RubyOutputStreamWrapper.wrap(getRubyRuntime(), (OutputStream) toFileOption));
        }

        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

        try {
            IRubyObject object = getAsciidoctorModule().callMethod("convert_file",
                    rubyRuntime.newString(file.toString()), rubyHash);
            return adaptReturn(object, expectedResult);
        } catch (RaiseException e) {
            logger.severe(e.getMessage());

            throw new AsciidoctorCoreException(e);
        } finally {
            // we restore current directory to its original value.
            rubyRuntime.setCurrentDirectory(currentDirectory);
        }
    }

    private <T> T adaptReturn(IRubyObject object, Class<T> expectedType) {
        if (NodeConverter.NodeType.DOCUMENT_CLASS.isInstance(object)) {
            if (Document.class.isAssignableFrom(expectedType)) {
                return (T) NodeConverter.createASTNode(object);
            }
            return null;
        } else if (RubyString.class.isInstance(object)) {
            if (String.class.isAssignableFrom(expectedType)) {
                return (T) object.asJavaString();
            }
            return null;
        }
        return RubyUtils.rubyToJava(rubyRuntime, object, expectedType);
    }

    @Override
    public String convertFile(File file, Options options) {
        return convertFile(file, options, String.class);
    }

    @Override
    public <T> T convertFile(File file, Options options, Class<T> expectedResult) {
        return convertFile(file, options.map(), expectedResult);
    }

    @Override
    public String[] convertDirectory(Iterable<File> directoryWalker, Options options) {
        List<String> asciidoctorContent = convertAllFiles(options, directoryWalker);
        return asciidoctorContent.toArray(new String[0]);
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, Options options) {
        List<String> asciidoctorContent = convertAllFiles(options, asciidoctorFiles);
        return asciidoctorContent.toArray(new String[0]);
    }

    @Override
    public Document load(String content, Options options) {
        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options.map());
        return (Document) NodeConverter.createASTNode(getAsciidoctorModule().callMethod("load",
                Optional.ofNullable(content).map(rubyRuntime::newString).orElse(null), rubyHash));
    }

    @Override
    public Document loadFile(File file, Options options) {
        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options.map());
        return (Document) NodeConverter.createASTNode(getAsciidoctorModule().callMethod("load_file",
                rubyRuntime.newString(file.getAbsolutePath()), rubyHash));
    }

    @Override
    public ExtensionGroup createGroup() {
        return createGroup(UUID.randomUUID().toString());
    }

    @Override
    public ExtensionGroup createGroup(String groupName) {
        return new ExtensionGroupImpl(groupName, this, getExtensionGroupClass());
    }

    private RubyClass getExtensionGroupClass() {
        if (this.extensionGroupClass == null) {
            extensionGroupClass = ExtensionGroupImpl.createExtensionGroupClass(rubyRuntime);
        }
        return extensionGroupClass;
    }

    @Override
    public void log(LogRecord logRecord) {
        for (LogHandler logHandler : logHandlers) {
            logHandler.log(logRecord);
        }
    }
}
