package org.asciidoctor.arquillian.api;

/**
 * A marker class passed to the {@link org.jboss.arquillian.test.api.ArquillianResource} annotation
 * to mark the {@link org.asciidoctor.Asciidoctor} instance as only created and used for a single test method.
 *
 * <p>Example for a field that is injected newly for every test method.</p>
 * <pre><code>
 *     &#64;ArquillianResource(Unshared.class)
 *     private Asciidoctor sharedAsciidoctor;
 * </code></pre>
 *
 * <p>Example for a parameter that is created only for this test method.</p>
 * <pre><code>
 *     public void test(&#64;ArquillianResource(Shared.class) Asciidoctor asciidoctor) {...}
 * </code></pre>
 */
public class Unshared {
}
