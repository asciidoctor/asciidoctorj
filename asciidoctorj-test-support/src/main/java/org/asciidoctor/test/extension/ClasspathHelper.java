package org.asciidoctor.test.extension;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Helps to get files from the classpath.
 */
public class ClasspathHelper {

    private final ClassLoader classloader;

    public ClasspathHelper(Class<?> cpReference) {
        this.classloader = cpReference.getClassLoader();
    }

    /**
     * Gets a resource in a similar way as {@link File#File(String)}.
     */
    public File getResource(String path) {
        return new File(getUri(path));
    }

    private URI getUri(String path) {
        try {
            URL resource = classloader.getResource(path);
            if (resource != null) {
                return classloader.getResource(path).toURI();
            } else {
                throw new RuntimeException(new FileNotFoundException(path));
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
