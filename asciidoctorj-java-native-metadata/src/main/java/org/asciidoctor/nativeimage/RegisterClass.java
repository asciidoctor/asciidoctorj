package org.asciidoctor.nativeimage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers classes for reflection in the GraalVM reachability metadata.
 * <p>
 * The {@link NativeMetadataProcessor} reads this annotation at compile time
 * and writes the registered classes to the metadata file.
 * The attributes select what is registered. They apply to all the classes of one annotation.
 * To register different things for different classes, repeat the annotation.
 * <p>
 * Example:
 * <pre>{@code
 * @RegisterClass({ Foo.class, Bar.class })
 * @RegisterClass(value = Baz.class, methods = true, fields = true)
 * public class MyType { }
 * }</pre>
 *
 * @see RegisterClasses
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(RegisterClasses.class)
public @interface RegisterClass {

    /**
     * The classes to register for reflection.
     *
     * @return the classes to register
     */
    Class<?>[] value();

    /**
     * Whether to register all the constructors of the classes.
     *
     * @return {@code true} to register all the constructors. The default is {@code true}.
     */
    boolean constructors() default true;

    /**
     * Whether to register all the public methods of the classes.
     * For a record, this also allows reflective access to the record components.
     *
     * @return {@code true} to register all the public methods. The default is {@code false}.
     */
    boolean methods() default false;

    /**
     * Whether to register all the declared fields of the classes.
     *
     * @return {@code true} to register all the declared fields. The default is {@code false}.
     */
    boolean fields() default false;
}
