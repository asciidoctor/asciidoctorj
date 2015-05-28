package org.asciidoctor.arquillian.api;

/**
 * A marker class passed to the {@link org.jboss.arquillian.test.api.ArquillianResource} annotation
 * to mark the {@link org.asciidoctor.Asciidoctor} instance as shared between multiple test methods.
 *
 * <p>Example for a field that is shared among multiple test methods</p>
 * <pre><code>
 *     &#64;ArquillianResource(Shared.class)
 *     private Asciidoctor sharedAsciidoctor;
 * </code></pre>
 *
 * <p>Example for a parameter that is shared among multiple test methods</p>
 * <pre><code>
 *     public void test(&#64;ArquillianResource(Shared.class) Asciidoctor asciidoctor) {...}
 * </code></pre>
 *
 * <p>Without using the {@code Shared} marker Asciidoctor instances are not shared and behave
 * as if the {@link Unshared} marker is used.</p>
 */
public class Shared {
}
