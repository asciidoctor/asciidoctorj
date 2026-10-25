package org.asciidoctor.core.extension;

/**
 * A Group is used to register one or more extensions with the Registry.
 *
 * <p>The Group should be implemented (or defined as a lambda) and registered with the Registry either by invoking the
 * {@link Extensions#register(Object)} method or passing the group to the extensions option. Extensions are registered
 * with the Registry inside the {@link #activate(Registry)} method.
 */
@FunctionalInterface
public interface Group {
    void activate(Registry registry);
}
