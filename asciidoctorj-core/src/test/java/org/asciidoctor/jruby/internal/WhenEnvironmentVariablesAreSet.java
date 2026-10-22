package org.asciidoctor.jruby.internal;

import org.hamcrest.MatcherAssert;
import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

public class WhenEnvironmentVariablesAreSet {

	@Test
	public void they_should_be_available_inside_ruby_engine() {
		try (JRubyAsciidoctor asciidoctor = JRubyAsciidoctor.create("My_gem_path")) {
			IRubyObject evalScriptlet = asciidoctor.rubyRuntime.evalScriptlet("ENV['GEM_PATH']");

			RubyString gemPathValue = (RubyString) evalScriptlet;
			MatcherAssert.assertThat(gemPathValue.asJavaString(), is("My_gem_path"));
		}
	}
	
}
