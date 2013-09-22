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
import org.asciidoctor.DocumentHeader;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Treeprocessor;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.javasupport.JavaEmbedUtils;

public class JRubyAsciidoctor implements Asciidoctor {

	private static final String GEM_PATH = "GEM_PATH";

	private AsciidoctorModule asciidoctorModule;
	protected RubyGemsPreloader rubyGemsPreloader;
	protected Ruby rubyRuntime;

	private JRubyAsciidoctor(AsciidoctorModule asciidoctorModule, Ruby rubyRuntime) {
		super();
		this.asciidoctorModule = asciidoctorModule;
		this.rubyRuntime = rubyRuntime;
		this.rubyGemsPreloader = new RubyGemsPreloader(this.rubyRuntime);
	}

	public static Asciidoctor create() {
		return createJRubyAsciidoctorInstance(new HashMap<String, Object>());
	}

	public static Asciidoctor create(String gemPath) {
		Map<String, Object> gemPathVar = new HashMap<String, Object>();
		gemPathVar.put(GEM_PATH, gemPath);

		Asciidoctor asciidoctor = createJRubyAsciidoctorInstance(gemPathVar);
		return asciidoctor;
	}

	private static Asciidoctor createJRubyAsciidoctorInstance(Map<String, Object> environmentVars) {

		RubyInstanceConfig config = createOptimizedConfiguration();

		Ruby rubyRuntime = JavaEmbedUtils.initialize(Collections.EMPTY_LIST, config);

		injectEnvironmentVariables(rubyRuntime, environmentVars);

		JRubyAsciidoctorModuleFactory jRubyAsciidoctorModuleFactory = new JRubyAsciidoctorModuleFactory(rubyRuntime);

		AsciidoctorModule asciidoctorModule = jRubyAsciidoctorModuleFactory.createAsciidoctorModule();

		JRubyAsciidoctor jRubyAsciidoctor = new JRubyAsciidoctor(asciidoctorModule, rubyRuntime);
		return jRubyAsciidoctor;
	}

	private static void injectEnvironmentVariables(Ruby runtime, Map<String, Object> environmentVars) {
		EnvironmentInjector environmentInjector = new EnvironmentInjector(runtime);
		environmentInjector.inject(environmentVars);
	}

	private static RubyInstanceConfig createOptimizedConfiguration() {
		RubyInstanceConfig config = new RubyInstanceConfig();
		config.setCompatVersion(CompatVersion.RUBY1_9);
		config.setCompileMode(CompileMode.OFF);

		return config;
	}

	private static DocumentHeader toDocumentHeader(DocumentRuby documentRuby) {
		return DocumentHeader.createDocumentHeader(documentRuby.doctitle(), documentRuby.title(), documentRuby.getAttributes());
	}

	@Override
	public DocumentHeader readDocumentHeader(File filename) {

		RubyHash rubyHash = getParseHeaderOnlyOption();

		DocumentRuby documentRuby = this.asciidoctorModule.load_file(filename.getAbsolutePath(), rubyHash);
		return toDocumentHeader(documentRuby);
	}

	@Override
	public DocumentHeader readDocumentHeader(String content) {

		RubyHash rubyHash = getParseHeaderOnlyOption();

		DocumentRuby documentRuby = this.asciidoctorModule.load(content, rubyHash);
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
		RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
		return rubyHash;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String render(String content, Map<String, Object> options) {

		this.rubyGemsPreloader.preloadRequiredLibraries(options);
		addRubyRuntimeAsAttribute(options);
		RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

		Object object = this.asciidoctorModule.render(content, rubyHash);
		return returnExpectedValue(object);

	}

    @SuppressWarnings("unchecked")
	@Override
	public String renderFile(File filename, Map<String, Object> options) {

		this.rubyGemsPreloader.preloadRequiredLibraries(options);

		addRubyRuntimeAsAttribute(options);
		
		RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
		
		Object object = this.asciidoctorModule.render_file(filename.getAbsolutePath(), rubyHash);
		return returnExpectedValue(object);

	}

    private void addRubyRuntimeAsAttribute(Map<String, Object> options) {
        if(options.containsKey(Options.ATTRIBUTES)) {
		    ((Map<String, Object>)options.get(Options.ATTRIBUTES)).put(Attributes.JRUBY, rubyRuntime);
		} else {
		    options.put(Options.ATTRIBUTES, new HashMap<String, Object>(){
		        {
		            put(Attributes.JRUBY, rubyRuntime);
		        }
		        });
		}
    }

	private Map<String, Object> removeCopyCssAttribute(Map<String, Object> attributes) {
		attributes.remove(Attributes.COPY_CSS);
		return attributes;
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
	public void render(Reader contentReader, Writer rendererWriter, Map<String, Object> options) throws IOException {
		String content = IOUtils.readFull(contentReader);
		String renderedContent = render(content, options);
		IOUtils.writeFull(rendererWriter, renderedContent);
	}

	@Override
	public String[] renderFiles(Collection<File> asciidoctorFiles, Map<String, Object> options) {
		List<String> asciidoctorContent = renderAllFiles(options, asciidoctorFiles);
		return asciidoctorContent.toArray(new String[asciidoctorContent.size()]);
	}

	@Override
	public String[] renderFiles(Collection<File> asciidoctorFiles, Options options) {
		return this.renderFiles(asciidoctorFiles, options.map());
	}

	@Override
	public String[] renderDirectory(DirectoryWalker directoryWalker, Map<String, Object> options) {

		final List<File> asciidoctorFiles = scanForAsciiDocFiles(directoryWalker);
		List<String> asciidoctorContent = renderAllFiles(options, asciidoctorFiles);

		return asciidoctorContent.toArray(new String[asciidoctorContent.size()]);
	}

	private List<String> renderAllFiles(Map<String, Object> options, final Collection<File> asciidoctorFiles) {
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
	public void render(Reader contentReader, Writer rendererWriter, Options options) throws IOException {
		this.render(contentReader, rendererWriter, options.map());
	}

	@Override
	public String renderFile(File filename, Options options) {
		return this.renderFile(filename, options.map());
	}

	@Override
	public String[] renderDirectory(DirectoryWalker directoryWalker, Options options) {
		return this.renderDirectory(directoryWalker, options.map());
	}

	@Override
	public String render(String content, OptionsBuilder options) {
		return this.render(content, options.asMap());
	}

	@Override
	public void render(Reader contentReader, Writer rendererWriter, OptionsBuilder options) throws IOException {
		this.render(contentReader, rendererWriter, options.asMap());
	}

	@Override
	public String renderFile(File filename, OptionsBuilder options) {
		return this.renderFile(filename, options.asMap());
	}

	@Override
	public String[] renderDirectory(DirectoryWalker directoryWalker, OptionsBuilder options) {
		return this.renderDirectory(directoryWalker, options.asMap());
	}

	@Override
	public String[] renderFiles(Collection<File> asciidoctorFiles, OptionsBuilder options) {
		return this.renderFiles(asciidoctorFiles, options.asMap());
	}
	
	@Override
	public void preprocessor(Class<? extends Preprocessor> preprocessor) {
	    //this may change in future to external class to deal with dynamic imports
	    this.rubyRuntime.evalScriptlet("java_import "+ preprocessor.getName());
	    this.asciidoctorModule.preprocessor(preprocessor.getSimpleName());
	}

    @Override
    public void postprocessor(Class<? extends Postprocessor> postprocesor) {
        //this may change in future to external class to deal with dynamic imports
        this.rubyRuntime.evalScriptlet("java_import "+ postprocesor.getName());
        this.asciidoctorModule.postprocessor(postprocesor.getSimpleName());
    }
    
    @Override
    public void includeProcessor(Class<? extends IncludeProcessor> includeProcessor) {
        //this may change in future to external class to deal with dynamic imports
        this.rubyRuntime.evalScriptlet("java_import "+ includeProcessor.getName());
        this.asciidoctorModule.include_processor(includeProcessor.getSimpleName());
    }
	
    @Override
    public void treeprocessor(Class<? extends Treeprocessor> treeProcessor) {
      //this may change in future to external class to deal with dynamic imports
        this.rubyRuntime.evalScriptlet("java_import "+ treeProcessor.getName());
        this.asciidoctorModule.treeprocessor(treeProcessor.getSimpleName());
    }
    
	@Override
	public void block(String blockName, Class<? extends BlockProcessor> blockProcessor) {
	  //this may change in future to external class to deal with dynamic imports
        this.rubyRuntime.evalScriptlet("java_import "+ blockProcessor.getName());
        this.asciidoctorModule.block(RubyUtils.toSymbol(rubyRuntime, blockName), blockProcessor.getSimpleName());
	}
	
	@Override
	public void block_macro(String blockName, Class<? extends BlockMacroProcessor> blockMacroProcessor) {
	  //this may change in future to external class to deal with dynamic imports
        this.rubyRuntime.evalScriptlet("java_import "+ blockMacroProcessor.getName());
        this.asciidoctorModule.block_macro(RubyUtils.toSymbol(rubyRuntime, blockName), blockMacroProcessor.getSimpleName());
	}
}
