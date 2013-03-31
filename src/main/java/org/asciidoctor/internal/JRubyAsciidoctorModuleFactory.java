package org.asciidoctor.internal;

import org.jruby.Ruby;
import org.jruby.RubyRuntimeAdapter;
import org.jruby.javasupport.JavaEmbedUtils;

class JRubyAsciidoctorModuleFactory {

	
	private static final String script = 
			"require 'java'\n" +
			"require 'asciidoctor'\n"+
			"class AsciidoctorModule\n" + 
			"  java_implements Java::Asciidoctor\n" + 
			"  def render_file(content, options = {})\n" + 
			"    return Asciidoctor.render_file(content, options)\n" + 
			"  end\n" + 
			"  def render(content, options = {})\n" + 
			"    return Asciidoctor.render(content, options)\n" + 
			"  end\n"+
			"  def load_file(content, options = {})\n" + 
			"    return Asciidoctor.load_file(content, options)\n" + 
			"  end\n" + 
			"  def load(content, options = {})\n" + 
			"    return Asciidoctor.load(content, options)\n" + 
			"  end\n" + 
			"end";
	
	private RubyRuntimeAdapter evaler;
	private Ruby runtime;
	
	public JRubyAsciidoctorModuleFactory(Ruby runtime) {
		this.runtime = runtime;
		this.evaler = JavaEmbedUtils.newRuntimeAdapter();
	}
	
	public AsciidoctorModule createAsciidoctorModule() {
		
		evaler.eval(runtime, script);
		Object rfj = evaler.eval(runtime, "AsciidoctorModule.new()");
		return RubyUtils.rubyToJava(runtime, (org.jruby.runtime.builtin.IRubyObject) rfj, AsciidoctorModule.class);
		
	}
	
	public Ruby runtime() {
		return this.runtime;
	}
	
}
