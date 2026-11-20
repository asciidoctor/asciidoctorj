package org.asciidoctor.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated resource will be initialized before the test is run.
 * <p>
 * The resource is a file or a directory in the classpath. In a GraalVM native image, the resources are not files in
 * the file system. In this case, the resource (and the content of a directory) is copied to a temporary directory,
 * and the annotated field or parameter gets the copy. The temporary directory is deleted when the JVM exits.
 * The resource must be included in the native image (in the <code>reachability-metadata.json</code> file),
 * and the copy is a new file: changes to it do not change the resource.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClasspathResource {

    String value();

}
