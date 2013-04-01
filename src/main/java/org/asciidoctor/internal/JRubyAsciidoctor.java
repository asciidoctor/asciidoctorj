package org.asciidoctor.internal;

import java.util.Collections;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Document;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;

public class JRubyAsciidoctor implements Asciidoctor {

	private AsciidoctorModule asciidoctorModule;

	private JRubyAsciidoctor(AsciidoctorModule asciidoctorModule) {
		super();
		this.asciidoctorModule = asciidoctorModule;
	}

	public static Asciidoctor create() {
		Ruby rubyRuntime = JavaEmbedUtils.initialize(Collections.EMPTY_LIST);
		
		JRubyAsciidoctorModuleFactory jRubyAsciidoctorModuleFactory = new JRubyAsciidoctorModuleFactory(
				rubyRuntime);

		AsciidoctorModule asciidoctorModule = jRubyAsciidoctorModuleFactory.createAsciidoctorModule();
		
		JRubyAsciidoctor jRubyAsciidoctor = new JRubyAsciidoctor(asciidoctorModule);
		return jRubyAsciidoctor;
	}

	@Override
	public Document load(String content, Map<Object, Object> options) {
		return  this.asciidoctorModule.load(content, options);
	}

	@Override
	public Document loadFile(String filename, Map<Object, Object> options) {
		return  this.asciidoctorModule.load_file(filename, options);
	}

	@Override
	public String render(String content, Map<Object, Object> options) {
		return this.asciidoctorModule.render(content, options);
	}

	@Override
	public String renderFile(String filename, Map<Object, Object> options) {
		return this.asciidoctorModule.render_file(filename, options);
	}

}
