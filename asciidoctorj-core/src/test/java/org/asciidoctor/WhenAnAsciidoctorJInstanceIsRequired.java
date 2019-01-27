package org.asciidoctor;

import org.asciidoctor.ruby.AsciidoctorJ;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class WhenAnAsciidoctorJInstanceIsRequired {

  private static final String DOC = "[yell]\nHello World";

  @Test
  public void shouldUnwrapAsciidoctorInstanceAndRegisterRubyExtension() throws Exception {
    AsciidoctorJ asciidoctorj = Asciidoctor.Factory.create().unwrap(AsciidoctorJ.class);
    asciidoctorj.rubyExtensionRegistry().loadClass(getClass().getResourceAsStream("/ruby-extensions/YellRubyBlock.rb")).block("yell", "YellRubyBlock");

    String html = asciidoctorj.convert(DOC, OptionsBuilder.options().headerFooter(false));

    assertThat(html, containsString("HELLO WORLD"));
  }

}
