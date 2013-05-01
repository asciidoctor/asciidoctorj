package org.asciidoctor.internal;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.asciidoctor.AsciiDocDirectoryWalker;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.DirectoryWalker;
import org.asciidoctor.Options;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.javasupport.JavaEmbedUtils;

public class JRubyAsciidoctor implements Asciidoctor {

	private AsciidoctorModule asciidoctorModule;
	private Ruby rubyRuntime;

	private JRubyAsciidoctor(AsciidoctorModule asciidoctorModule, Ruby rubyRuntime) {
		super();
		this.asciidoctorModule = asciidoctorModule;
		this.rubyRuntime = rubyRuntime;
	}

	
	public static Asciidoctor create() {
		
		RubyInstanceConfig config = createOptimizedConfiguration();
		
		Ruby rubyRuntime = JavaEmbedUtils.initialize(Collections.EMPTY_LIST, config);
		
		JRubyAsciidoctorModuleFactory jRubyAsciidoctorModuleFactory = new JRubyAsciidoctorModuleFactory(
				rubyRuntime);

		AsciidoctorModule asciidoctorModule = jRubyAsciidoctorModuleFactory.createAsciidoctorModule();
		
		JRubyAsciidoctor jRubyAsciidoctor = new JRubyAsciidoctor(asciidoctorModule, rubyRuntime);
		return jRubyAsciidoctor;
	}


	private static RubyInstanceConfig createOptimizedConfiguration() {
		RubyInstanceConfig config = new RubyInstanceConfig();   
		config.setCompatVersion(CompatVersion.RUBY1_9);
		config.setCompileMode(CompileMode.OFF);

		return config;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String render(String content, Map<String, Object> options) {
		
		RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
		return this.asciidoctorModule.render(content, rubyHash);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String renderFile(File filename, Map<String, Object> options) {
		
		RubyHash rubyHash = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
		Object object = this.asciidoctorModule.render_file(filename.getAbsolutePath(), rubyHash);
		
		return returnExpectedValue(object);
		
	}


	/**
	 * This method has been added to deal with the fact that asciidoctor 0.1.2 can return an Asciidoctor::Document or a String depending if content is write to disk or not.
	 * This may change in the future (https://github.com/asciidoctor/asciidoctor/issues/286) 
	 * @param object
	 * @return
	 */
	private String returnExpectedValue(Object object) {
		if(object instanceof String) {
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
	public String[] renderDirectory(File directory, Map<String, Object> options) {
		
		final List<File> asciidoctorFiles = scanForAsciiDocFiles(directory);
		List<String> asciidoctorContent = renderAllFiles(options, asciidoctorFiles);
		
		return asciidoctorContent.toArray(new String[asciidoctorContent.size()]);
	}


	private List<String> renderAllFiles(Map<String, Object> options, final List<File> asciidoctorFiles) {
		List<String> asciidoctorContent = new ArrayList<String>();
		
		for (File asciidoctorFile : asciidoctorFiles) {
			String renderedFile = renderFile(asciidoctorFile, options);
			
			if(renderedFile != null) {
				asciidoctorContent.add(renderedFile);
			}
			
		}
		
		return asciidoctorContent;
	}


	private List<File> scanForAsciiDocFiles(File directory) {
		final DirectoryWalker directoryWalker = new AsciiDocDirectoryWalker(directory.getAbsolutePath());
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
	public String[] renderDirectory(File directory, Options options) {
		return this.renderDirectory(directory, options.map());
	}

}
