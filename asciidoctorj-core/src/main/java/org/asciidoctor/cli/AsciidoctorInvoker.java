package org.asciidoctor.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.DirectoryWalker;
import org.asciidoctor.GlobDirectoryWalker;
import org.asciidoctor.Options;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.internal.JRubyRuntimeContext;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.RubySymbol;

import com.beust.jcommander.JCommander;
import org.jruby.runtime.builtin.IRubyObject;

public class AsciidoctorInvoker {

    public void invoke(String... parameters) {

        AsciidoctorCliOptions asciidoctorCliOptions = new AsciidoctorCliOptions();
        JCommander jCommander = new JCommander(asciidoctorCliOptions,
                parameters);

        if (asciidoctorCliOptions.isHelp() || parameters.length == 0) {
            jCommander.setProgramName("asciidoctor");
            jCommander.usage();
        } else {

            Asciidoctor asciidoctor = buildAsciidoctorJInstance(asciidoctorCliOptions);
            
            if (asciidoctorCliOptions.isVersion()) {
                System.out.println("Asciidoctor " + asciidoctor.asciidoctorVersion() + " [http://asciidoctor.org]");
                return;
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

            Options options = asciidoctorCliOptions.parse();
            
            if(asciidoctorCliOptions.isRequire()) {
                for (String require : asciidoctorCliOptions.getRequire()) {
                    RubyUtils.requireLibrary(JRubyRuntimeContext.get(), require);
                }
            }

            setTimingsMode(asciidoctorCliOptions, options);

            setVerboseLevel(asciidoctorCliOptions);
            
            String output = renderInput(asciidoctor, options, inputFiles);

            if (asciidoctorCliOptions.isTimings()) {

                Map<String, Object> optionsMap = options.map();
                IRubyObject timings = (IRubyObject) optionsMap.get("timings");
                timings.callMethod(JRubyRuntimeContext.get().getCurrentContext(), "print_report");
            }

            if (!"".equals(output.trim())) {
                System.out.println(output);
            }
        }
    }

    private void setTimingsMode(AsciidoctorCliOptions asciidoctorCliOptions, Options options) {
        if (asciidoctorCliOptions.isTimings()) {
            options.setOption("timings",
                JRubyRuntimeContext.get().evalScriptlet("Asciidoctor::Timings.new"));
        }
    }

    private void setVerboseLevel(AsciidoctorCliOptions asciidoctorCliOptions) {
        if(asciidoctorCliOptions.isVerbose()) {
            RubyUtils.setGlobalVariable(JRubyRuntimeContext.get(), "VERBOSE", "true");
        } else {
            if(asciidoctorCliOptions.isQuiet()) {
                RubyUtils.setGlobalVariable(JRubyRuntimeContext.get(), "VERBOSE", "nil");
            }
        }
    }

    private Asciidoctor buildAsciidoctorJInstance(AsciidoctorCliOptions asciidoctorCliOptions) {
        Asciidoctor asciidoctor;
        if (asciidoctorCliOptions.isLoadPaths()) {
            asciidoctor = JRubyAsciidoctor.create(asciidoctorCliOptions.getLoadPaths());   
        } else {
            asciidoctor = JRubyAsciidoctor.create((String) null);
        }
        return asciidoctor;
    }

    private String renderInput(Asciidoctor asciidoctor, Options options, List<File> inputFiles) {
        

        // jcommander bug makes this code not working.
        // if("-".equals(inputFile)) {
        // return asciidoctor.render(readInputFromStdIn(), options);
        // }

        StringBuilder output = new StringBuilder();

        for (File inputFile : inputFiles) {

            if (inputFile.canRead()) {

                String renderedFile = asciidoctor
                        .renderFile(inputFile, options);
                if (renderedFile != null) {
                    output.append(renderedFile).append(
                            System.getProperty("line.separator"));
                }
            } else {
                System.err.println("asciidoctor: FAILED: input file(s) '"
                        + inputFile.getAbsolutePath()
                        + "' missing or cannot be read");
                throw new IllegalArgumentException(
                        "asciidoctor: FAILED: input file(s) '"
                                + inputFile.getAbsolutePath()
                                + "' missing or cannot be read");
            }
        }

        return output.toString();
    }

    private String readInputFromStdIn() {
        Scanner in = new Scanner(System.in);
        String content = in.nextLine();
        in.close();

        return content;
    }

    private List<File> getInputFiles(AsciidoctorCliOptions asciidoctorCliOptions) {

        List<String> parameters = asciidoctorCliOptions.getParameters();

        if (parameters.isEmpty()) {
            System.err.println("asciidoctor: FAILED: input file missing");
            throw new IllegalArgumentException(
                    "asciidoctor: FAILED: input file missing");
        }

        if (parameters.contains("-")) {
            System.err
                    .println("asciidoctor:  FAILED: input file is required instead of an argument.");
            throw new IllegalArgumentException(
                    "asciidoctor:  FAILED: input file is required instead of an argument.");
        }

        List<File> filesToBeRendered = new ArrayList<File>();

        for (String globExpression : parameters) {
            DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(globExpression);
            filesToBeRendered.addAll(globDirectoryWalker.scan());
        }

        return filesToBeRendered;

    }

    public static void main(String args[]) {
        new AsciidoctorInvoker().invoke(args);
    }

}
