package org.asciidoctor.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.asciidoctor.cli.Invoker;
import org.asciidoctor.core.api.ast.NodeConverter;

/**
 * The base class of the CLI tests, which provides the CLI helpers of the Ruby test suite (test_helper.rb).
 */
public abstract class CliTestHelper extends TestHelper {

    /**
     * @return the node of the implementation wrapped by the node of the AsciidoctorJ API (e.g., the document returned
     * by the invoker)
     */
    public static <T extends AbstractNode> T unwrap(Object node) {
        return NodeConverter.unwrap(node);
    }
    /**
     * @return the command (and arguments) to run the CLI in a separate process
     */
    public static List<String> asciidoctorCmd(List<String> jvmArgs) {
        List<String> cmd = new ArrayList<>();
        cmd.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        if (jvmArgs != null) {
            cmd.addAll(jvmArgs);
        }
        cmd.addAll(List.of("-cp", System.getProperty("java.class.path"), Invoker.class.getName()));
        return cmd;
    }

    public static List<String> asciidoctorCmd() {
        return asciidoctorCmd(null);
    }

    public static Invoker invokeCliWithFilenames(List<String> argv, List<String> filenames, Supplier<?> stdin) {
        List<String> args = new ArrayList<>(argv);
        for (String filename : filenames) {
            args.add(Path.of(filename).isAbsolute() ? filename : fixturePath(filename));
        }
        Invoker invoker = new Invoker(args);
        invoker.invoke(stdin);
        return invoker;
    }

    public static Invoker invokeCliWithFilenames(List<String> argv, List<String> filenames) {
        return invokeCliWithFilenames(argv, filenames, null);
    }

    public static Invoker invokeCliToBuffer(List<String> argv, String filename, Supplier<?> stdin) {
        return invokeCli(argv, filename, List.of(new StringBuilder(), new StringBuilder()), stdin);
    }

    public static Invoker invokeCliToBuffer(List<String> argv, String filename) {
        return invokeCliToBuffer(argv, filename, null);
    }

    public static Invoker invokeCliToBuffer(List<String> argv, Supplier<?> stdin) {
        return invokeCliToBuffer(argv, "sample.adoc", stdin);
    }

    public static Invoker invokeCliToBuffer(List<String> argv) {
        return invokeCliToBuffer(argv, "sample.adoc", null);
    }

    public static Invoker invokeCliToBuffer() {
        return invokeCliToBuffer(List.of());
    }

    /**
     * Invokes the CLI with the arguments and the file (resolved from the fixtures directory if relative).
     *
     * @param buffers the targets of the output and error streams, or null to use the standard streams
     * @param stdin the function that supplies the standard input, or null
     */
    public static Invoker invokeCli(List<String> argv, String filename, List<? extends Appendable> buffers,
            Supplier<?> stdin) {
        // NOTE the filename may be a glob pattern, which is not a valid path on Windows
        String filepath = filename == null || filename.equals("-") || new PathResolver().isAbsolutePath(filename) ? filename
                : fixturePath(filename);
        List<String> args = new ArrayList<>(argv);
        if (filepath != null) {
            args.add(filepath);
        }
        Invoker invoker = new Invoker(args);
        if (buffers != null) {
            invoker.redirectStreams(buffers.get(0), buffers.size() > 1 ? buffers.get(1) : null);
        }
        invoker.invoke(stdin);
        return invoker;
    }

    public static Invoker invokeCli(List<String> argv, String filename, Supplier<?> stdin) {
        return invokeCli(argv, filename, null, stdin);
    }

    public static Invoker invokeCli(List<String> argv, String filename) {
        return invokeCli(argv, filename, null, null);
    }

    public static Invoker invokeCli(List<String> argv, Supplier<?> stdin) {
        return invokeCli(argv, "sample.adoc", null, stdin);
    }

    public static Invoker invokeCli(List<String> argv) {
        return invokeCli(argv, "sample.adoc", null, null);
    }

    public static Invoker invokeCli() {
        return invokeCli(List.of());
    }
}
