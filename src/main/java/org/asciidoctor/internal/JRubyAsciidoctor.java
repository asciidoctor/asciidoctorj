package org.asciidoctor.internal;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.DirectoryWalker;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.ast.StructuredDocument;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.RubyExtensionRegistry;
import org.asciidoctor.extension.internal.ExtensionRegistryExecutor;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.javasupport.JavaEmbedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JRubyAsciidoctor implements Asciidoctor {

	private static final Logger log = LoggerFactory
			.getLogger(JRubyAsciidoctor.class.getName());

	private static final String GEM_PATH = "GEM_PATH";

	private static final int DEFAULT_MAX_LEVEL = 1;

	private AsciidoctorModule asciidoctorModule;
	protected RubyGemsPreloader rubyGemsPreloader;
	protected Ruby rubyRuntime;

	private JRubyAsciidoctor(AsciidoctorModule asciidoctorModule,
			Ruby rubyRuntime) {
		super();
		this.asciidoctorModule = asciidoctorModule;
		this.rubyRuntime = rubyRuntime;
		this.rubyGemsPreloader = new RubyGemsPreloader(this.rubyRuntime);
	}

	public static Asciidoctor create() {
		Asciidoctor asciidoctor = createJRubyAsciidoctorInstance(new HashMap<String, Object>());
		registerExtensions(asciidoctor);

		return asciidoctor;
	}

	public static Asciidoctor create(String gemPath) {
		Map<String, Object> gemPathVar = new HashMap<String, Object>();
		gemPathVar.put(GEM_PATH, gemPath);

		Asciidoctor asciidoctor = createJRubyAsciidoctorInstance(gemPathVar);
		registerExtensions(asciidoctor);

		return asciidoctor;
	}

	public static Asciidoctor create(List<String> loadPaths) {
		Asciidoctor asciidoctor = createJRubyAsciidoctorInstance(loadPaths);
		registerExtensions(asciidoctor);

		return asciidoctor;
	}

	private static void registerExtensions(Asciidoctor asciidoctor) {
		new ExtensionRegistryExecutor(asciidoctor).registerAllExtensions();
	}

	private static Asciidoctor createJRubyAsciidoctorInstance(
			List<String> loadPaths) {

		RubyInstanceConfig config = createOptimizedConfiguration();

		Ruby rubyRuntime = JavaEmbedUtils.initialize(loadPaths, config);
		JRubyRuntimeContext.set(rubyRuntime);

		JRubyAsciidoctorModuleFactory jRubyAsciidoctorModuleFactory = new JRubyAsciidoctorModuleFactory(
				rubyRuntime);

		AsciidoctorModule asciidoctorModule = jRubyAsciidoctorModuleFactory
				.createAsciidoctorModule();
		JRubyAsciidoctor jRubyAsciidoctor = new JRubyAsciidoctor(
				asciidoctorModule, rubyRuntime);

		return jRubyAsciidoctor;
	}

	private static Asciidoctor createJRubyAsciidoctorInstance(
			Map<String, Object> environmentVars) {

		RubyInstanceConfig config = createOptimizedConfiguration();

		Ruby rubyRuntime = JavaEmbedUtils.initialize(Collections.EMPTY_LIST,
				config);

		injectEnvironmentVariables(rubyRuntime, environmentVars);

		JRubyRuntimeContext.set(rubyRuntime);

		JRubyAsciidoctorModuleFactory jRubyAsciidoctorModuleFactory = new JRubyAsciidoctorModuleFactory(
				rubyRuntime);

		AsciidoctorModule asciidoctorModule = jRubyAsciidoctorModuleFactory
				.createAsciidoctorModule();

		JRubyAsciidoctor jRubyAsciidoctor = new JRubyAsciidoctor(
				asciidoctorModule, rubyRuntime);
		return jRubyAsciidoctor;
	}

	private static void injectEnvironmentVariables(Ruby runtime,
			Map<String, Object> environmentVars) {
		EnvironmentInjector environmentInjector = new EnvironmentInjector(
				runtime);
		environmentInjector.inject(environmentVars);
	}

	private static RubyInstanceConfig createOptimizedConfiguration() {
		RubyInstanceConfig config = new RubyInstanceConfig();
		config.setCompatVersion(CompatVersion.RUBY1_9);
		config.setCompileMode(CompileMode.OFF);

		return config;
	}

	private static DocumentHeader toDocumentHeader(DocumentRuby documentRuby) {
		return DocumentHeader.createDocumentHeader(documentRuby.doctitle(),
				documentRuby.title(), documentRuby.getAttributes());
	}

	private StructuredDocument toDocument(DocumentRuby documentRuby,
			Ruby rubyRuntime, int maxDeepLevel) {

		Document document = new Document(documentRuby, rubyRuntime);
		List<ContentPart> contentParts = getContents(document.blocks(), 1,
				maxDeepLevel);
		return StructuredDocument.createStructuredDocument(
				toDocumentHeader(documentRuby), contentParts);
	}

	private List<ContentPart> getContents(List<Block> blocks, int level,
			int maxDeepLevel) {
		// finish getting childs if max structure level was riched
		if (level > maxDeepLevel) {
			return null;
		}
		// if document has only one child don't treat as actual contentpart
		// unless
		// it has no childs
		/*if (blocks.size() == 1 && blocks.get(0).blocks().size() > 0) {
			return getContents(blocks.get(0).blocks(), 0, maxDeepLevel);
		}*/
		// add next level of contentParts
		List<ContentPart> parts = new ArrayList<ContentPart>();
		for (Block block : blocks) {
			parts.add(getContentPartFromBlock(block, level, maxDeepLevel));
		}
		return parts;
	}

	private ContentPart getContentPartFromBlock(Block child, int level,
			int maxDeepLevel) {
		Object content = child.content();
		String textContent;
		if (content instanceof String) {
			textContent = (String) content;
		} else {
			textContent = child.render();
		}
		ContentPart contentPart = ContentPart.createContentPart(child.id(),
				level, child.context(), child.title(), child.style(),
				child.role(), child.attributes(), textContent);
		contentPart.setParts(getContents(child.blocks(), level + 1,
				maxDeepLevel));
		return contentPart;
	}

	@SuppressWarnings("unchecked")
	@Override
	public StructuredDocument readDocumentStructure(File filename,
			Map<String, Object> options) {

		this.rubyGemsPreloader.preloadRequiredLibraries(options);

		RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(
				rubyRuntime, options);
		DocumentRuby documentRuby = this.asciidoctorModule.load_file(
				filename.getAbsolutePath(), rubyHash);
		int maxDeepLevel = options.containsKey(STRUCTURE_MAX_LEVEL) ? (Integer) (options
				.get(STRUCTURE_MAX_LEVEL)) : DEFAULT_MAX_LEVEL;
		return toDocument(documentRuby, rubyRuntime, maxDeepLevel);
	}

	@SuppressWarnings("unchecked")
	@Override
	public StructuredDocument readDocumentStructure(String content,
			Map<String, Object> options) {

		this.rubyGemsPreloader.preloadRequiredLibraries(options);

		RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(
				rubyRuntime, options);

		DocumentRuby documentRuby = this.asciidoctorModule.load(content,
				rubyHash);
		int maxDeepLevel = options.containsKey(STRUCTURE_MAX_LEVEL) ? (Integer) (options
				.get(STRUCTURE_MAX_LEVEL)) : DEFAULT_MAX_LEVEL;
		return toDocument(documentRuby, rubyRuntime, maxDeepLevel);
	}

	@Override
	public StructuredDocument readDocumentStructure(Reader contentReader,
			Map<String, Object> options) {
		String content = IOUtils.readFull(contentReader);
		return readDocumentStructure(content, options);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentHeader readDocumentHeader(File filename) {

		RubyHash rubyHash = getParseHeaderOnlyOption();

		DocumentRuby documentRuby = this.asciidoctorModule.load_file(
				filename.getAbsolutePath(), rubyHash);
		return toDocumentHeader(documentRuby);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DocumentHeader readDocumentHeader(String content) {

		RubyHash rubyHash = getParseHeaderOnlyOption();

		DocumentRuby documentRuby = this.asciidoctorModule.load(content,
				rubyHash);
		return toDocumentHeader(documentRuby);
	}

	@Override
	public DocumentHeader readDocumentHeader(Reader contentReader) {
		String content = IOUtils.readFull(contentReader);
		return this.readDocumentHeader(content);
	}

	private RubyHash getParseHeaderOnlyOption() {
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("parse_header_only", true);
		RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(
				rubyRuntime, options);
		return rubyHash;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String render(String content, Map<String, Object> options) {

		this.rubyGemsPreloader.preloadRequiredLibraries(options);

		if (log.isDebugEnabled()) {
			log.debug(AsciidoctorUtils.toAsciidoctorCommand(options, "-"));

			if (AsciidoctorUtils.isOptionWithAttribute(options,
					Attributes.SOURCE_HIGHLIGHTER, "pygments")) {
				log.debug("In order to use Pygments with Asciidoctor, you need to install Pygments (and Python, if you don’t have it yet). Read http://asciidoctor.org/news/#syntax-highlighting-with-pygments.");
			}
		}

		String currentDirectory = rubyRuntime.getCurrentDirectory();

		if (options.containsKey(Options.BASEDIR)) {
			rubyRuntime.setCurrentDirectory((String) options
					.get(Options.BASEDIR));
		}

		RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(
				rubyRuntime, options);

		Object object = this.asciidoctorModule.render(content, rubyHash);

		// we restore current directory to its original value.
		rubyRuntime.setCurrentDirectory(currentDirectory);

		return returnExpectedValue(object);

	}

	@SuppressWarnings("unchecked")
	@Override
	public String renderFile(File filename, Map<String, Object> options) {

		this.rubyGemsPreloader.preloadRequiredLibraries(options);

		if (log.isDebugEnabled()) {
			log.debug(AsciidoctorUtils.toAsciidoctorCommand(options,
					filename.getAbsolutePath()));
		}

		String currentDirectory = rubyRuntime.getCurrentDirectory();

		if (options.containsKey(Options.BASEDIR)) {
			rubyRuntime.setCurrentDirectory((String) options
					.get(Options.BASEDIR));
		}

		RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(
				rubyRuntime, options);

		Object object = this.asciidoctorModule.render_file(
				filename.getAbsolutePath(), rubyHash);

		// we restore current directory to its original value.
		rubyRuntime.setCurrentDirectory(currentDirectory);

		return returnExpectedValue(object);

	}

	/**
	 * This method has been added to deal with the fact that asciidoctor 0.1.2
	 * can return an Asciidoctor::Document or a String depending if content is
	 * write to disk or not. This may change in the future
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
	public void render(Reader contentReader, Writer rendererWriter,
			Map<String, Object> options) throws IOException {
		String content = IOUtils.readFull(contentReader);
		String renderedContent = render(content, options);
		IOUtils.writeFull(rendererWriter, renderedContent);
	}

	@Override
	public String[] renderFiles(Collection<File> asciidoctorFiles,
			Map<String, Object> options) {
		List<String> asciidoctorContent = renderAllFiles(options,
				asciidoctorFiles);
		return asciidoctorContent
				.toArray(new String[asciidoctorContent.size()]);
	}

	@Override
	public String[] renderFiles(Collection<File> asciidoctorFiles,
			Options options) {
		return this.renderFiles(asciidoctorFiles, options.map());
	}

	@Override
	public String[] renderDirectory(DirectoryWalker directoryWalker,
			Map<String, Object> options) {

		final List<File> asciidoctorFiles = scanForAsciiDocFiles(directoryWalker);
		List<String> asciidoctorContent = renderAllFiles(options,
				asciidoctorFiles);

		return asciidoctorContent
				.toArray(new String[asciidoctorContent.size()]);
	}

	private List<String> renderAllFiles(Map<String, Object> options,
			final Collection<File> asciidoctorFiles) {
		List<String> asciidoctorContent = new ArrayList<String>();

		for (File asciidoctorFile : asciidoctorFiles) {
			String renderedFile = renderFile(asciidoctorFile, options);

			if (renderedFile != null) {
				asciidoctorContent.add(renderedFile);
			}

		}

		return asciidoctorContent;
	}

	private List<File> scanForAsciiDocFiles(DirectoryWalker directoryWalker) {
		final List<File> asciidoctorFiles = directoryWalker.scan();
		return asciidoctorFiles;
	}

	@Override
	public String render(String content, Options options) {
		return this.render(content, options.map());
	}

	@Override
	public void render(Reader contentReader, Writer rendererWriter,
			Options options) throws IOException {
		this.render(contentReader, rendererWriter, options.map());
	}

	@Override
	public String renderFile(File filename, Options options) {
		return this.renderFile(filename, options.map());
	}

	@Override
	public String[] renderDirectory(DirectoryWalker directoryWalker,
			Options options) {
		return this.renderDirectory(directoryWalker, options.map());
	}

	@Override
	public String render(String content, OptionsBuilder options) {
		return this.render(content, options.asMap());
	}

	@Override
	public void render(Reader contentReader, Writer rendererWriter,
			OptionsBuilder options) throws IOException {
		this.render(contentReader, rendererWriter, options.asMap());
	}

	@Override
	public String renderFile(File filename, OptionsBuilder options) {
		return this.renderFile(filename, options.asMap());
	}

	@Override
	public String[] renderDirectory(DirectoryWalker directoryWalker,
			OptionsBuilder options) {
		return this.renderDirectory(directoryWalker, options.asMap());
	}

	@Override
	public String[] renderFiles(Collection<File> asciidoctorFiles,
			OptionsBuilder options) {
		return this.renderFiles(asciidoctorFiles, options.asMap());
	}

	@Override
	public JavaExtensionRegistry javaExtensionRegistry() {
		return new JavaExtensionRegistry(asciidoctorModule, rubyRuntime);
	}

	@Override
	public RubyExtensionRegistry rubyExtensionRegistry() {
		return new RubyExtensionRegistry(asciidoctorModule, rubyRuntime);
	}

	@Override
	public void unregisterAllExtensions() {
		this.asciidoctorModule.unregister_all_extensions();
	}

	@Override
	public void shutdown() {
		this.rubyRuntime.tearDown();
	}

	@Override
	public String asciidoctorVersion() {
		return this.asciidoctorModule.asciidoctorRuntimeEnvironmentVersion();
	}

}
