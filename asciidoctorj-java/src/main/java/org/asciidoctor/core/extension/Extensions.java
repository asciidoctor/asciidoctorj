package org.asciidoctor.core.extension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.asciidoctor.core.Helpers;

/**
 * Extensions provide a way to participate in the parsing and converting phases of the AsciiDoc processor or extend the
 * AsciiDoc syntax.
 *
 * <p>The various extensions participate in AsciiDoc processing as follows:
 *
 * <ol>
 * <li>After the source lines are normalized, {@link Preprocessor}s modify or replace the source lines before parsing
 * begins. {@link IncludeProcessor}s are used to process include directives for targets which they claim to handle.</li>
 * <li>The Parser parses the block-level content into an abstract syntax tree. Custom blocks and block macros are
 * processed by associated {@link BlockProcessor}s and {@link BlockMacroProcessor}s, respectively.</li>
 * <li>{@link TreeProcessor}s are run on the abstract syntax tree.</li>
 * <li>Conversion of the document begins, at which point inline markup is processed and converted. Custom inline
 * macros are processed by associated {@link InlineMacroProcessor}s.</li>
 * <li>{@link Postprocessor}s modify or replace the converted document.</li>
 * <li>The output is written to the output stream.</li>
 * </ol>
 *
 * <p>Extensions may be registered globally using the {@link #register(Object)} method or added to a custom
 * {@link Registry} instance and passed as an option to a single Asciidoctor processor.
 */
public final class Extensions {
    private static final AtomicInteger AUTO_ID = new AtomicInteger(-1);
    private static Map<String, Object> groups = new LinkedHashMap<>();

    private Extensions() {
    }

    public static String generateName() {
        return "extgrp" + nextAutoId();
    }

    public static int nextAutoId() {
        return AUTO_ID.incrementAndGet();
    }

    /**
     * @return the Map of statically-registered extension groups.
     */
    public static synchronized Map<String, Object> getGroups() {
        return groups;
    }

    /**
     * Creates a new Registry, optionally seeded with the specified group.
     *
     * @param name the name of the group (optional)
     * @param group the Group to register with the new registry (optional)
     * @return a new Registry
     */
    public static Registry create(String name, Group group) {
        if (group != null) {
            Map<String, Object> seed = new LinkedHashMap<>();
            seed.put(name != null ? name : generateName(), group);
            return new Registry(seed);
        }
        return new Registry();
    }

    public static Registry create(Group group) {
        return create(null, group);
    }

    public static Registry create() {
        return new Registry();
    }

    /**
     * Registers an extension Group that subsequently registers a collection of extensions.
     *
     * <p>Registers the extension Group specified under the given name. If a name is not given, one is calculated by
     * appending the next value in a 0-based index to the string "extgrp". For instance, the first unnamed extension
     * group to be registered is assigned the name "extgrp0" if a name is not specified.
     *
     * <p>The names are not yet used, but are intended for selectively activating extensions in the future.
     *
     * <p>If the extension group argument is a String, it gets resolved to a Class before being registered.
     *
     * <pre>{@code
     * Extensions.register(UmlExtensions.class);
     *
     * Extensions.register("uml", UmlExtensions.class);
     *
     * Extensions.register(registry -> registry.block("plantuml", PlantUmlBlock.class));
     * }</pre>
     *
     * @param name The name under which this extension group is registered (optional, default: null)
     * @param group A Group (e.g., a lambda), a Class or String name of a Class that implements Group.
     * @return the group, matching the type passed to this method.
     */
    public static synchronized Object register(String name, Object group) {
        if (group == null) {
            throw new IllegalArgumentException("Extension group to register not specified");
        }
        Object resolvedGroup = group instanceof String ? Helpers.resolveClass(group) : group;
        groups.put(name != null ? name : generateName(), resolvedGroup);
        return resolvedGroup;
    }

    public static Object register(Object group) {
        return register(null, group);
    }

    /**
     * Registers an extension Group (typically a lambda).
     */
    public static Group register(Group group) {
        register(null, (Object) group);
        return group;
    }

    public static Group register(String name, Group group) {
        register(name, (Object) group);
        return group;
    }

    /**
     * Unregister all statically-registered extension groups.
     */
    public static synchronized void unregisterAll() {
        groups = new LinkedHashMap<>();
    }

    /**
     * Unregister statically-registered extension groups by name.
     *
     * @param names one or more String group names to unregister
     */
    public static synchronized void unregister(String... names) {
        for (String name : names) {
            groups.remove(name);
        }
    }
}
