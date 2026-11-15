package org.asciidoctor.nativeimage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Registers files as resources in the GraalVM reachability metadata. Each value is a path or a glob (for example "data/**.json"), and adds one glob.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RegisterFile {

    String[] value();
}
