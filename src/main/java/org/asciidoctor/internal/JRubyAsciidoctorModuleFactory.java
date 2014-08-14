package org.asciidoctor.internal;

import java.io.InputStream;

import org.jruby.Ruby;
import org.jruby.RubyRuntimeAdapter;
import org.jruby.javasupport.JavaEmbedUtils;

class JRubyAsciidoctorModuleFactory {

    // hack: to fix problem with copycss this should change in future.
    protected RubyRuntimeAdapter evaler;
    private Ruby runtime;

    public JRubyAsciidoctorModuleFactory(Ruby runtime) {
        this.runtime = runtime;
        this.evaler = JavaEmbedUtils.newRuntimeAdapter();
    }

    public AsciidoctorModule createAsciidoctorModule() {
        // This piece of code will be changed in future when asciidoctor gem implements a class instead of a module.
        String script = loadAsciidoctorRubyClass();
        evaler.eval(runtime, script);

        Object rfj = evaler.eval(runtime, "AsciidoctorModule.new()");

        return RubyUtils.rubyToJava(runtime, (org.jruby.runtime.builtin.IRubyObject) rfj, AsciidoctorModule.class);

    }

    private String loadAsciidoctorRubyClass() {
        InputStream inputStream = JRubyAsciidoctorModuleFactory.class.getResourceAsStream("asciidoctorclass.rb");
        return IOUtils.readFull(inputStream);
    }

    public Ruby runtime() {
        return this.runtime;
    }

}
