package org.asciidoctor.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;


public class ClassPathHelper {

    private ClassLoader classloader;

    public File getResource (String pathname) {
        try {
            URL resouce = classloader.getResource(pathname);
            if (resouce != null)
                return new File(classloader.getResource(pathname).toURI());
            else
                throw new RuntimeException(new FileNotFoundException(pathname));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public File getResource (String parent, String child) {
        return new File (getResource(parent), child);
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
