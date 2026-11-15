package org.asciidoctor.nativeimage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Registers directories (and all their content) as resources in the GraalVM reachability metadata. Each directory adds two globs: "dir" and "dir/**". A trailing slash is removed.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RegisterDirectory {

    String[] value();
}
