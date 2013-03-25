package org.asciidoctor.internal;

import java.util.Collections;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Document;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;

public class JRubyAsciidoctor implements Asciidoctor {

	private Ruby runtime;
	private AsciidoctorModule asciidoctorModule;

	private JRubyAsciidoctor() {
		super();
	}

	public static Asciidoctor create() {
		Ruby rubyRuntime = JavaEmbedUtils.initialize(Collections.EMPTY_LIST);
		
		JRubyAsciidoctorModuleFactory jRubyAsciidoctorModuleFactory = new JRubyAsciidoctorModuleFactory(
				rubyRuntime);

		AsciidoctorModule asciidoctorModule = jRubyAsciidoctorModuleFactory.createAsciidoctorModule();
		
		JRubyAsciidoctor jRubyAsciidoctor = new JRubyAsciidoctor();
		jRubyAsciidoctor.asciidoctorModule = asciidoctorModule;
		jRubyAsciidoctor.runtime = rubyRuntime;
		
		return jRubyAsciidoctor;
	}

	@Override
	public Document load(String content, Map<Object, Object> options) {
		Object document = this.asciidoctorModule.load(content, options);
		return RubyUtils.rubyToJava(runtime, (org.jruby.runtime.builtin.IRubyObject) document, Document.class);
	}

	@Override
	public Document load_file(String filename, Map<Object, Object> options) {
		Object document = this.asciidoctorModule.load_file(filename, options);
		return RubyUtils.rubyToJava(runtime, (org.jruby.runtime.builtin.IRubyObject) document, Document.class);
	}

	@Override
	public String render(String content, Map<Object, Object> options) {
		return this.asciidoctorModule.render(content, options);
	}

	@Override
	public String render_file(String filename, Map<Object, Object> options) {
		return this.asciidoctorModule.render_file(filename, options);
	}

}
