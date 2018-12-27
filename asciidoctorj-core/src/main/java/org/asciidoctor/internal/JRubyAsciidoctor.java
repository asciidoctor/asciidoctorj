package org.asciidoctor.internal;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.api.Attributes;
import org.asciidoctor.api.DirectoryWalker;
import org.asciidoctor.api.Options;
import org.asciidoctor.api.OptionsBuilder;
import org.asciidoctor.api.ast.Document;
import org.asciidoctor.api.ast.DocumentHeader;
import org.asciidoctor.ast.NodeConverter;
import org.asciidoctor.ast.impl.DocumentHeaderImpl;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.converter.internal.ConverterRegistryExecutor;
import org.asciidoctor.api.extension.ExtensionGroup;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.RubyExtensionRegistry;
import org.asciidoctor.extension.internal.ExtensionRegistryExecutor;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.internal.JULLogHandler;
import org.asciidoctor.log.internal.JavaLogger;
import org.asciidoctor.log.internal.LogHandlerRegistryExecutor;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyModule;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JRubyAsciidoctor implements Asciidoctor, LogHandler {

    private static final Logger logger = Logger.getLogger(JRubyAsciidoctor.class.getName());

    private static final String GEM_PATH = "GEM_PATH";

    private static final int DEFAULT_MAX_LEVEL = 1;

    protected RubyGemsPreloader rubyGemsPreloader;
    protected Ruby rubyRuntime;

    private RubyClass extensionGroupClass;

    private List<LogHandler> logHandlers = new ArrayList<>();

    private final static Logger LOGGER = Logger.getLogger("asciidoctorj");

    private JRubyAsciidoctor(final Ruby rubyRuntime) {
        this.rubyRuntime = rubyRuntime;

        InputStream inputStream = getClass().getResourceAsStream("asciidoctorclass.rb");
        final String script = IOUtils.readFull(inputStream);
        this.rubyRuntime.evalScriptlet(script);

        this.rubyGemsPreloader = new RubyGemsPreloader(this.rubyRuntime);
        this.logHandlers.add(new JULLogHandler());
    }

    public static JRubyAsciidoctor create() {
        return create((String) null);
    }

    public static JRubyAsciidoctor create(String gemPath) {
        return processRegistrations(createJRubyAsciidoctorInstance(Collections.singletonMap(GEM_PATH, gemPath), new ArrayList<String>(), null));
    }

    public static JRubyAsciidoctor create(List<String> loadPaths) {
        return processRegistrations(createJRubyAsciidoctorInstance(null, loadPaths, null));
    }

    public static JRubyAsciidoctor create(ClassLoader classloader) {
        return processRegistrations(createJRubyAsciidoctorInstance(null, new ArrayList<String>(), classloader));
    }

    public static JRubyAsciidoctor create(ClassLoader classloader, String gemPath) {
        return processRegistrations(createJRubyAsciidoctorInstance(Collections.singletonMap(GEM_PATH, gemPath), new ArrayList<String>(), classloader));
    }

    public static JRubyAsciidoctor create(List<String> loadPaths, String gemPath) {
        return processRegistrations(createJRubyAsciidoctorInstance(Collections.singletonMap(GEM_PATH, gemPath), loadPaths, null));
    }

    private static JRubyAsciidoctor processRegistrations(JRubyAsciidoctor asciidoctor) {
        registerExtensions(asciidoctor);
        registerConverters(asciidoctor);
        registerLogHandlers(asciidoctor);
        return asciidoctor;
    }

    private static void registerConverters(Asciidoctor asciidoctor) {
        new ConverterRegistryExecutor(asciidoctor).registerAllConverters();
    }

    private static void registerExtensions(Asciidoctor asciidoctor) {
        new ExtensionRegistryExecutor(asciidoctor).registerAllExtensions();
    }

    private static void registerLogHandlers(Asciidoctor asciidoctor) {
        new LogHandlerRegistryExecutor(asciidoctor).registerAllLogHandlers();
    }

    private static JRubyAsciidoctor createJRubyAsciidoctorInstance(Map<String, String> environmentVars, List<String> loadPaths, ClassLoader classloader) {

        Map<String, String> env = environmentVars != null ?
                new HashMap<String, String>(environmentVars) : new HashMap<String, String>();

        RubyInstanceConfig config = createOptimizedConfiguration();
        if (classloader != null) {
            config.setLoader(classloader);
        }
        injectEnvironmentVariables(config, env);

        Ruby rubyRuntime = JavaEmbedUtils.initialize(loadPaths, config);

        JRubyAsciidoctor jrubyAsciidoctor = new JRubyAsciidoctor(rubyRuntime);

        JavaLogger.install(rubyRuntime, jrubyAsciidoctor);

        return jrubyAsciidoctor;
    }

    private static void injectEnvironmentVariables(RubyInstanceConfig config, Map<String, String> environmentVars) {
        EnvironmentInjector environmentInjector = new EnvironmentInjector(config);
        environmentInjector.inject(environmentVars);
    }

    private static RubyInstanceConfig createOptimizedConfiguration() {
        return new RubyInstanceConfig();
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

    private DocumentHeader toDocumentHeader(Document document) {

        Document documentImpl = (Document) NodeConverter.createASTNode(document);

        return DocumentHeaderImpl.createDocumentHeader(documentImpl.getStructuredDoctitle(), documentImpl.getDoctitle(),
                documentImpl.getAttributes());
    }

    @SuppressWarnings("unchecked")
    @Override
    public DocumentHeader readDocumentHeader(File filename) {

        RubyHash rubyHash = getParseHeaderOnlyOption();

        Document document = (Document) NodeConverter.createASTNode(getAsciidoctorModule().callMethod("load_file", rubyRuntime.newString(filename.getAbsolutePath()), rubyHash));

        return toDocumentHeader(document);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DocumentHeader readDocumentHeader(String content) {

        RubyHash rubyHash = getParseHeaderOnlyOption();

        Document document = (Document) NodeConverter.createASTNode(getAsciidoctorModule().callMethod("load", rubyRuntime.newString(content), rubyHash));
        return toDocumentHeader(document);
    }

    @Override
    public DocumentHeader readDocumentHeader(Reader contentReader) {
        String content = IOUtils.readFull(contentReader);
        return this.readDocumentHeader(content);
    }

    private RubyHash getParseHeaderOnlyOption() {
        Map<String, Object> options = new HashMap<>();
        options.put("parse_header_only", true);
        return RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public String render(String content, Map<String, Object> options) {
        return convert(content, options);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public String renderFile(File filename, Map<String, Object> options) {
        return convertFile(filename, options);
    }

    /**
     * This method has been added to deal with the fact that asciidoctor 0.1.2 can return an Asciidoctor::Document or a
     * String depending if content is write to disk or not. This may change in the future
     * (https://github.com/asciidoctor/asciidoctor/issues/286)
     * 
     * @param object
     * @return
     */
    private String returnExpectedValue(Object object) {
        if (object instanceof String) {
            return object.toString();
        } else {
            return null;
        }
    }

    @Override
    @Deprecated
    public void render(Reader contentReader, Writer rendererWriter, Map<String, Object> options) throws IOException {
       convert(contentReader, rendererWriter, options);
    }

    @Override
    @Deprecated
    public String[] renderFiles(Collection<File> asciidoctorFiles, Map<String, Object> options) {
        return convertFiles(asciidoctorFiles, options);
    }

    @Override
    @Deprecated
    public String[] renderFiles(Collection<File> asciidoctorFiles, Options options) {
        return this.convertFiles(asciidoctorFiles, options.map());
    }

    @Override
    @Deprecated
    public String[] renderDirectory(DirectoryWalker directoryWalker, Map<String, Object> options) {
        return convertDirectory(directoryWalker, options);
    }

    private List<String> convertAllFiles(Map<String, Object> options, final Collection<File> asciidoctorFiles) {
        List<String> asciidoctorContent = new ArrayList<>();
        for (File asciidoctorFile : asciidoctorFiles) {
            String renderedFile = convertFile(asciidoctorFile, options);
            if (renderedFile != null) {
                asciidoctorContent.add(renderedFile);
            }
        }
        return asciidoctorContent;
    }

    private List<File> scanForAsciiDocFiles(DirectoryWalker directoryWalker) {
        return directoryWalker.scan();
    }

    @Override
    @Deprecated
    public String render(String content, Options options) {
        return convert(content, options.map());
    }

    @Override
    @Deprecated
    public void render(Reader contentReader, Writer rendererWriter, Options options) throws IOException {
        convert(contentReader, rendererWriter, options.map());
    }

    @Override
    @Deprecated
    public String renderFile(File filename, Options options) {
        return convertFile(filename, options.map());
    }

    @Override
    @Deprecated
    public String[] renderDirectory(DirectoryWalker directoryWalker, Options options) {
        return convertDirectory(directoryWalker, options.map());
    }

    @Override
    @Deprecated
    public String render(String content, OptionsBuilder options) {
        return convert(content, options.asMap());
    }

    @Override
    @Deprecated
    public void render(Reader contentReader, Writer rendererWriter, OptionsBuilder options) throws IOException {
        convert(contentReader, rendererWriter, options.asMap());
    }

    @Override
    @Deprecated
    public String renderFile(File filename, OptionsBuilder options) {
        return convertFile(filename, options.asMap());
    }

    @Override
    @Deprecated
    public String[] renderDirectory(DirectoryWalker directoryWalker, OptionsBuilder options) {
        return convertDirectory(directoryWalker, options.asMap());
    }

    @Override
    @Deprecated
    public String[] renderFiles(Collection<File> asciidoctorFiles, OptionsBuilder options) {
        return convertFiles(asciidoctorFiles, options.asMap());
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
        return new JavaExtensionRegistryImpl(rubyRuntime);
    }

    @Override
    public RubyExtensionRegistry rubyExtensionRegistry() {
        return new RubyExtensionRegistryImpl(rubyRuntime);
    }

    @Override
    public JavaConverterRegistry javaConverterRegistry() {
        return new JavaConverterRegistryImpl(rubyRuntime);
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

    @Override
    public String convert(String content, Map<String, Object> options) {
        return convert(content, options, String.class);
    }

    public <T> T convert(String content, Map<String, Object> options, Class<T> expectedResult) {

        this.rubyGemsPreloader.preloadRequiredLibraries(options);

        logger.fine(AsciidoctorUtils.toAsciidoctorCommand(options, "-"));

        if (AsciidoctorUtils.isOptionWithAttribute(options, Attributes.SOURCE_HIGHLIGHTER, "pygments")) {
            logger.fine("In order to use Pygments with Asciidoctor, you need to install Pygments (and Python, if you don't have it yet). Read http://asciidoctor.org/news/#syntax-highlighting-with-pygments.");
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
                rubyRuntime.newString(content), rubyHash);
            if (NodeConverter.NodeType.DOCUMENT_CLASS.isInstance(object)) {
                // If a document is rendered to a file Asciidoctor returns the document, we return null
                return null;
            }
            return RubyUtils.rubyToJava(rubyRuntime, object, expectedResult);
        } catch(RaiseException e) {
            logger.severe(e.getException().getClass().getCanonicalName());
            throw new AsciidoctorCoreException(e);
        } finally {
            // we restore current directory to its original value.
            rubyRuntime.setCurrentDirectory(currentDirectory);
        }

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
    public String convert(String content, OptionsBuilder options) {
        return convert(content, options, String.class);
    }

    @Override
    public <T> T convert(String content, OptionsBuilder options, Class<T> expectedResult) {
        return convert(content, options.asMap(), expectedResult);
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, Map<String, Object> options) throws IOException {
        String content = IOUtils.readFull(contentReader);
        String renderedContent = convert(content, options);
        IOUtils.writeFull(rendererWriter, renderedContent);
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, Options options) throws IOException {
        this.convert(contentReader, rendererWriter, options.map());
    }

    @Override
    public void convert(Reader contentReader, Writer rendererWriter, OptionsBuilder options) throws IOException {
        this.convert(contentReader, rendererWriter, options.asMap());
    }

    @Override
    public String convertFile(File filename, Map<String, Object> options) {
        return convertFile(filename, options, String.class);
    }

    @Override
    public <T> T convertFile(File filename, Map<String, Object> options, Class<T> expectedResult) {

        this.rubyGemsPreloader.preloadRequiredLibraries(options);

        logger.fine(AsciidoctorUtils.toAsciidoctorCommand(options, filename.getAbsolutePath()));

        String currentDirectory = rubyRuntime.getCurrentDirectory();

        if (options.containsKey(Options.BASEDIR)) {
            rubyRuntime.setCurrentDirectory((String) options.get(Options.BASEDIR));
        }

        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

        try {
            IRubyObject object = getAsciidoctorModule().callMethod("convert_file",
                rubyRuntime.newString(filename.getAbsolutePath()), rubyHash);
            if (NodeConverter.NodeType.DOCUMENT_CLASS.isInstance(object)) {
                // If a document is rendered to a file Asciidoctor returns the document, we return null
                return null;
            }
            return RubyUtils.rubyToJava(rubyRuntime, object, expectedResult);
        } catch(RaiseException e) {
            logger.severe(e.getMessage());

            throw new AsciidoctorCoreException(e);
        } finally {
            // we restore current directory to its original value.
            rubyRuntime.setCurrentDirectory(currentDirectory);
        }
    }

    @Override
    public String convertFile(File filename, Options options) {
        return convertFile(filename, options, String.class);
    }

    @Override
    public <T> T convertFile(File filename, Options options, Class<T> expectedResult) {
        return convertFile(filename, options.map(), expectedResult);
    }

    @Override
    public String convertFile(File filename, OptionsBuilder options) {
        return convertFile(filename, options.asMap(), String.class);
    }

    @Override
    public <T> T convertFile(File filename, OptionsBuilder options, Class<T> expectedResult) {
        return convertFile(filename, options.asMap(), expectedResult);
    }

    @Override
    public String[] convertDirectory(DirectoryWalker directoryWalker, Map<String, Object> options) {
        final List<File> asciidoctorFiles = scanForAsciiDocFiles(directoryWalker);
        List<String> asciidoctorContent = convertAllFiles(options, asciidoctorFiles);
        return asciidoctorContent.toArray(new String[0]);
    }

    @Override
    public String[] convertDirectory(DirectoryWalker directoryWalker, Options options) {
        return convertDirectory(directoryWalker, options.map());
    }

    @Override
    public String[] convertDirectory(DirectoryWalker directoryWalker, OptionsBuilder options) {
        return convertDirectory(directoryWalker, options.asMap());
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, Map<String, Object> options) {
        List<String> asciidoctorContent = convertAllFiles(options, asciidoctorFiles);
        return asciidoctorContent.toArray(new String[0]);
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, Options options) {
        return convertFiles(asciidoctorFiles, options.map());
    }

    @Override
    public String[] convertFiles(Collection<File> asciidoctorFiles, OptionsBuilder options) {
        return convertFiles(asciidoctorFiles, options.asMap());
    }

    @Override
    public Document load(String content, Map<String, Object> options) {
        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
        return (Document) NodeConverter.createASTNode(getAsciidoctorModule().callMethod("load",
            rubyRuntime.newString(content), rubyHash));
    }

    @Override
    public Document loadFile(File file, Map<String, Object> options) {
        RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

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
        for (LogHandler logHandler: logHandlers) {
            try {
                logHandler.log(logRecord);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unexpected exception while logging Asciidoctor log entry", e);
            }
        }
    }
}
