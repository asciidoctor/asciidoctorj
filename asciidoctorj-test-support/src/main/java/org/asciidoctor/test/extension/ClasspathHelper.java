package org.asciidoctor.test.extension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Helps to get files from the classpath.
 */
public class ClasspathHelper {

    private static Path temporaryDirectory;

    private final ClassLoader classloader;

    public ClasspathHelper(Class<?> cpReference) {
        this.classloader = cpReference.getClassLoader();
    }

    /**
     * Gets a resource in a similar way as {@link File#File(String)}.
     * <p>
     * In a GraalVM native image, the resources are not files in the file system: a copy of the resource (and of its
     * content, for a directory) is created in a temporary directory, and this copy is returned.
     */
    public File getResource(String path) {
        URL resource = classloader.getResource(path);
        if (resource == null) {
            throw new RuntimeException(new FileNotFoundException(path));
        }
        if (isNativeImage() && !"file".equals(resource.getProtocol())) {
            return copyToTemporaryDirectory(path);
        }
        return new File(getUri(resource));
    }

    private static boolean isNativeImage() {
        return System.getProperty("org.graalvm.nativeimage.imagecode") != null;
    }

    private URI getUri(URL resource) {
        try {
            return resource.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private File copyToTemporaryDirectory(String path) {
        try {
            final Path target = temporaryDirectory().resolve(path);
            copy(path, target);
            return target.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void copy(String path, Path target) throws IOException {
        final byte[] content;
        try (InputStream stream = classloader.getResourceAsStream(path)) {
            if (stream == null) {
                throw new FileNotFoundException(path);
            }
            content = stream.readAllBytes();
        }
        final List<String> children = listChildren(path, content);
        if (children.isEmpty()) {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } else {
            Files.createDirectories(target);
            for (String child : children) {
                copy(path + "/" + child, target.resolve(child));
            }
        }
    }

    /**
     * The content of a directory resource is the list of its entries, one per line.
     * The content is a list of children only if all the lines are resources in this directory.
     */
    private List<String> listChildren(String path, byte[] content) {
        final List<String> children = new ArrayList<>();
        for (String line : new String(content, StandardCharsets.UTF_8).split("\\R")) {
            if (line.isEmpty() || classloader.getResource(path + "/" + line) == null) {
                return Collections.emptyList();
            }
            children.add(line);
        }
        return children;
    }

    private static synchronized Path temporaryDirectory() throws IOException {
        if (temporaryDirectory == null) {
            temporaryDirectory = Files.createTempDirectory("asciidoctorj-classpath");
            final Path directory = temporaryDirectory;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteRecursively(directory)));
        }
        return temporaryDirectory;
    }

    private static void deleteRecursively(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        } catch (IOException e) {
            // the directory is temporary: ignore the error
        }
    }
}
