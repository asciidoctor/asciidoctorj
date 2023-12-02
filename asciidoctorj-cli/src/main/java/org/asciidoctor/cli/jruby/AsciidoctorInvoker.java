package org.asciidoctor.cli.jruby;

import com.beust.jcommander.JCommander;
import org.asciidoctor.cli.AsciidoctorCliOptions;
import org.asciidoctor.cli.MaxSeverityLogHandler;
import org.asciidoctor.jruby.DirectoryWalker;
import org.asciidoctor.jruby.GlobDirectoryWalker;
import org.asciidoctor.jruby.internal.IOUtils;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;
import org.asciidoctor.jruby.internal.JRubyRuntimeContext;
import org.asciidoctor.jruby.internal.RubyGemsPreloader;
import org.asciidoctor.jruby.internal.RubyUtils;
import org.jruby.Main;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class AsciidoctorInvoker {

    public int invoke(String... parameters) throws IOException {

        AsciidoctorCliOptions asciidoctorCliOptions = new AsciidoctorCliOptions();
        JCommander jCommander = new JCommander(asciidoctorCliOptions);
        jCommander.parse(parameters);

        if (asciidoctorCliOptions.isHelp() || parameters.length == 0) {
            jCommander.setProgramName("asciidoctor");
            jCommander.usage();
        } else {

            JRubyAsciidoctor asciidoctor = buildAsciidoctorJInstance(asciidoctorCliOptions);

            if (asciidoctorCliOptions.isVersion()) {
                System.out.println("AsciidoctorJ " + getAsciidoctorJVersion() + " (Asciidoctor " + asciidoctor.asciidoctorVersion() + ") [https://asciidoctor.org]");
                Object rubyVersionString = JRubyRuntimeContext.get(asciidoctor).evalScriptlet("\"#{JRUBY_VERSION} (#{RUBY_VERSION})\"");
                System.out.println("Runtime Environment: jruby " + rubyVersionString);
                return 0;
            }

            List<File> inputFiles = getInputFiles(asciidoctorCliOptions);

            if (inputFiles.isEmpty()) {
                System.err.println("asciidoctor: FAILED: input file(s) '"
                        + asciidoctorCliOptions.getParameters()
                        + "' missing or cannot be read");
                throw new IllegalArgumentException(
                        "asciidoctor: FAILED: input file(s) '"
                                + asciidoctorCliOptions.getParameters()
                                + "' missing or cannot be read");
            }

            MaxSeverityLogHandler maxSeverityLogHandler = new MaxSeverityLogHandler();
            asciidoctor.registerLogHandler(maxSeverityLogHandler);

            if (asciidoctorCliOptions.isRequire()) {
                for (String require : asciidoctorCliOptions.getRequire()) {
                    RubyUtils.requireLibrary(asciidoctor.getRubyRuntime(), require);
                }
            }

            setVerboseLevel(asciidoctor, asciidoctorCliOptions);

            convertInput(asciidoctor, asciidoctorCliOptions, inputFiles);

            if (asciidoctorCliOptions.getFailureLevel().compareTo(maxSeverityLogHandler.getMaxSeverity()) <= 0) {
                return 1;
            }

        }
        return 0;
    }

    private String getAsciidoctorJVersion() {
        InputStream in = getClass().getResourceAsStream("/META-INF/asciidoctorj-version.properties");
        Properties versionProps = new Properties();
        try {
            versionProps.load(in);
            return versionProps.getProperty("version.asciidoctorj");
        } catch (IOException e) {
            return "N/A";
        }
    }

    private void setVerboseLevel(JRubyAsciidoctor asciidoctor, AsciidoctorCliOptions asciidoctorCliOptions) {
        if (asciidoctorCliOptions.isVerbose()) {
            RubyUtils.setGlobalVariable(asciidoctor.getRubyRuntime(), "VERBOSE", "true");
        } else {
            if (asciidoctorCliOptions.isQuiet()) {
                RubyUtils.setGlobalVariable(asciidoctor.getRubyRuntime(), "VERBOSE", "nil");
            }
        }
    }

    private JRubyAsciidoctor buildAsciidoctorJInstance(AsciidoctorCliOptions asciidoctorCliOptions) {
        ClassLoader oldTccl = Thread.currentThread().getContextClassLoader();
        try {
            if (asciidoctorCliOptions.isClassPaths()) {
                URLClassLoader tccl = createUrlClassLoader(asciidoctorCliOptions.getClassPaths());
                Thread.currentThread().setContextClassLoader(tccl);
            }
            JRubyAsciidoctor asciidoctor;
            if (asciidoctorCliOptions.isLoadPaths()) {
                asciidoctor = JRubyAsciidoctor.create(asciidoctorCliOptions.getLoadPaths());
            } else {
                asciidoctor = JRubyAsciidoctor.create();
            }
            return asciidoctor;
        } finally {
            Thread.currentThread().setContextClassLoader(oldTccl);
        }
    }

    private URLClassLoader createUrlClassLoader(List<String> classPaths) {
        List<URL> cpUrls = new ArrayList<>();
        for (String cp : classPaths) {
            try {
                DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(cp);
                for (File f : globDirectoryWalker.scan()) {
                    cpUrls.add(f.toURI().toURL());
                }
            } catch (Exception e) {
                System.err.println(String.format("asciidoctor: WARNING: Could not resolve classpath '%s': %s", cp, e.getMessage()));
            }
        }
        return new URLClassLoader(cpUrls.toArray(new URL[cpUrls.size()]));
    }

    private void convertInput(JRubyAsciidoctor asciidoctor, AsciidoctorCliOptions cliOptions, List<File> inputFiles) throws IOException {
        Ruby ruby = JRubyRuntimeContext.get(asciidoctor);
        RubyHash opts = cliOptions.parse(ruby);

        new RubyGemsPreloader(asciidoctor.getRubyRuntime()).preloadRequiredLibrariesCommandLine(opts);

        opts.put(ruby.newSymbol("input_files"), inputFiles.stream().map(f -> ruby.newString(f.getPath())).collect(Collectors.toList()));

        RubyClass invokerClass = ruby.getModule("AsciidoctorJ").getModule("Cli").getClass("Invoker");
        IRubyObject invoker = invokerClass.newInstance(ruby.getCurrentContext(), opts);
        invoker.callMethod(ruby.getCurrentContext(), "invoke!");
    }

    private Optional<File> findInvalidInputFile(List<File> inputFiles) {
        return inputFiles.stream()
                .filter(inputFile -> !inputFile.canRead())
                .findFirst();
    }

    private String readInputFromStdIn() {
        return IOUtils.readFull(System.in);
    }

    private List<File> getInputFiles(AsciidoctorCliOptions asciidoctorCliOptions) {

        List<String> parameters = asciidoctorCliOptions.getParameters();

        if (parameters.stream().anyMatch(String::isEmpty)) {
            System.err.println("asciidoctor: FAILED: empty input file name");
            throw new IllegalArgumentException("asciidoctor: FAILED: empty input file name");
        }

        return parameters.stream()
                .map(globExpression -> new GlobDirectoryWalker(globExpression))
                .flatMap(walker -> walker.scan().stream())
                .collect(Collectors.toList());
    }

    public static void main(String args[]) throws IOException {

        // Process .jrubyrc file
        Main.processDotfile();

        int status = new AsciidoctorInvoker().invoke(args);
        if (status != 0) {
            System.exit(status);
        }
    }

}
