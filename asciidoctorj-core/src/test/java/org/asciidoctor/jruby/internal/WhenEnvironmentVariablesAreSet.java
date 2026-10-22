package org.asciidoctor.jruby.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

public class WhenEnvironmentVariablesAreSet {

	@Test
	public void they_should_be_available_inside_ruby_engine() {
		
		JRubyAsciidoctor asciidoctor = (JRubyAsciidoctor) JRubyAsciidoctor.create("My_gem_path");
		IRubyObject evalScriptlet = asciidoctor.rubyRuntime.evalScriptlet("ENV['GEM_PATH']");
		
		RubyString gemPathValue = (RubyString)evalScriptlet;
		assertThat(gemPathValue.asJavaString(), is("My_gem_path"));
		
		
	}
	
}
