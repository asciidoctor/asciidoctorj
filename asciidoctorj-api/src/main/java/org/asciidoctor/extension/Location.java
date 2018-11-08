package org.asciidoctor.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines the location where the content created by a DocinfoProcessor will be added.
 * <p>The following example will add a robots meta tag to the head element of the resulting HTML document:
 * <pre>
 * <ode>
 * &#64;Location(LocationType.HEADER)
 * class RobotsDocinfoProcessor extends DocinfoProcessor {
 *
 *     public static final String META_TAG = "&lt;meta name=\"robots\" content=\"index, follow\"/&gt;";
 *
 *     public String process(Document document) {
 *         return META_TAG;
 *     }
 * }
 * </ode>
 * </pre>
 * <p>Applicable for:
 * <table>
 * <tr><td>BlockMacroProcessor</td><td></td></tr>
 * <tr><td>BlockProcessor</td><td></td></tr>
 * <tr><td>BlockProcessor</td><td></td></tr>
 * <tr><td>DocInfoProcessor</td><td>&#10003;</td></tr>
 * <tr><td>IncludeProcessor</td><td></td></tr>
 * <tr><td>InlineMacroProcessor</td><td></td></tr>
 * <tr><td>Postprocessor</td><td></td></tr>
 * <tr><td>Preprocessor</td><td></td></tr>
 * <tr><td>Treeprocessor</td><td></td></tr>
 * </table>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Location {

    LocationType value() default LocationType.HEADER;

}
