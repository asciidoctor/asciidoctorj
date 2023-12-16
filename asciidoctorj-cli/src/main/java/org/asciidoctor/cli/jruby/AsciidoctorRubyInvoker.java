package org.asciidoctor.cli.jruby;

import org.asciidoctor.cli.AsciidoctorCliOptions;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.jruby.internal.RubyGemsPreloader;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts AsciiDoc files using the Asciidoctor Ruby API.
 * Note that this bypasses the AsciidoctorJ API and is only used for the CLI.
 */
public class AsciidoctorRubyInvoker {

    private final JRubyAsciidoctor asciidoctor;

    private final Ruby ruby;

    private final RubyClass invokerClass;

    public AsciidoctorRubyInvoker(JRubyAsciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor;
        this.ruby = asciidoctor.getRubyRuntime();
        this.invokerClass = ruby.getModule("AsciidoctorJ").getModule("Cli").getClass("Invoker");
    }

    public void invoke(List<File> inputFiles, AsciidoctorCliOptions cliOptions) {
        RubyHash opts = cliOptions.parse(ruby);

        new RubyGemsPreloader(asciidoctor.getRubyRuntime()).preloadRequiredLibraries(opts);

        opts.put(ruby.newSymbol("input_files"), inputFiles.stream().map(f -> ruby.newString(f.getPath())).collect(Collectors.toList()));

        IRubyObject invoker = invokerClass.newInstance(ruby.getCurrentContext(), opts);
        invoker.callMethod(ruby.getCurrentContext(), "invoke!");
    }

}
