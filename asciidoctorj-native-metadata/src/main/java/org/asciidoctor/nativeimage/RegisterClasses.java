package org.asciidoctor.nativeimage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// The container of the repeated annotations RegisterClass
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RegisterClasses {

    RegisterClass[] value();
}
