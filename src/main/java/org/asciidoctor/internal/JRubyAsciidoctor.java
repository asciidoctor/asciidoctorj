package org.asciidoctor.internal;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.jruby.Ruby;
import org.jruby.RubyHash;
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
		Ruby rubyRuntime = JavaEmbedUtils.initialize(Collections.EMPTY_LIST);
		
		JRubyAsciidoctorModuleFactory jRubyAsciidoctorModuleFactory = new JRubyAsciidoctorModuleFactory(
				rubyRuntime);

		AsciidoctorModule asciidoctorModule = jRubyAsciidoctorModuleFactory.createAsciidoctorModule();
		
		JRubyAsciidoctor jRubyAsciidoctor = new JRubyAsciidoctor(asciidoctorModule, rubyRuntime);
		return jRubyAsciidoctor;
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
		return this.asciidoctorModule.render_file(filename.getAbsolutePath(), rubyHash);
		
	}

}
