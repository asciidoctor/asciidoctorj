package org.asciidoctor.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Helps to get files from the classpath.
 */
public class ClasspathHelper {

    private ClassLoader classloader;

    /**
     * Gets a resource in a similar way as {@link File#File(String)}
     */
    public File getResource(String pathname) {
        try {
            URL resource = classloader.getResource(pathname);
            if (resource != null) {
                return new File(classloader.getResource(pathname).toURI());
            } else {
                throw new RuntimeException(new FileNotFoundException(pathname));
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Gets a resource in a similar way as {@link File#File(String, String)}
     */
    public File getResource(String parent, String child) {
        return new File(getResource(parent), child);
    }

    public ClassLoader getClassloader() {
        return classloader;
    }

    public void setClassloader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public void setClassloader(Class<?> clazz) {
        this.classloader = clazz.getClassLoader();
    }
}
