package org.asciidoctor.core.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.core.ContentModel;
import org.asciidoctor.core.Strings;

/**
 * The base class for processors that handle custom syntax (blocks, block macros and inline macros). It provides the
 * DSL methods to configure the name, content model and attributes of the processor.
 */
public abstract class SyntaxProcessor extends Processor {
    protected String name;

    protected SyntaxProcessor(String name, Map<String, Object> config) {
        super(config);
        this.name = name != null ? name : Strings.valueOf(this.config.get("name"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the name of this processor (DSL).
     */
    public SyntaxProcessor named(String value) {
        this.name = value;
        return this;
    }

    /**
     * Sets the content model of this processor (DSL).
     */
    public SyntaxProcessor contentModel(ContentModel value) {
        option("content_model", value);
        return this;
    }

    public SyntaxProcessor parseContentAs(ContentModel value) {
        return contentModel(value);
    }

    /**
     * Sets the names to assign to the positional attributes (DSL).
     */
    public SyntaxProcessor positionalAttributes(String... value) {
        option("positional_attrs", new ArrayList<>(Arrays.asList(value)));
        return this;
    }

    public SyntaxProcessor positionalAttributes(List<String> value) {
        option("positional_attrs", new ArrayList<>(value));
        return this;
    }

    /**
     * Sets the default attributes (DSL).
     */
    public SyntaxProcessor defaultAttributes(Map<String, Object> value) {
        option("default_attrs", value);
        return this;
    }

    /**
     * Resolves the positional attribute names and default attributes from the specified specifications (DSL).
     *
     * <p>Each specification may be a name (e.g., "alt"), a name with a value (e.g., "alt=Image"), a name with an index
     * (e.g., "1:alt") or a name with an index and a value (e.g., "1:alt=Image"). The index "@" refers to the next
     * position.
     */
    public SyntaxProcessor resolveAttributes(String... specs) {
        List<String> names = new ArrayList<>();
        Map<String, Object> defaults = new LinkedHashMap<>();
        for (String arg : specs) {
            if (arg.contains("=")) {
                String[] parts = Strings.partition(arg, "=");
                String attrName = parts[0];
                if (attrName.contains(":")) {
                    String[] idxParts = Strings.partition(attrName, ":");
                    attrName = idxParts[2];
                    setAt(names, idxParts[0].equals("@") ? names.size() : Strings.toInt(idxParts[0]), attrName);
                }
                defaults.put(attrName, parts[2]);
            } else if (arg.contains(":")) {
                String[] idxParts = Strings.partition(arg, ":");
                setAt(names, idxParts[0].equals("@") ? names.size() : Strings.toInt(idxParts[0]), idxParts[2]);
            } else {
                names.add(arg);
            }
        }
        names.removeIf(java.util.Objects::isNull);
        option("positional_attrs", names);
        option("default_attrs", defaults);
        return this;
    }

    /**
     * Resolves the positional attribute names and default attributes from the specified Map (DSL). The keys are the
     * names (optionally prefixed by an index, e.g., 1:alt) and the values are the default values (or null).
     */
    public SyntaxProcessor resolveAttributes(Map<String, Object> spec) {
        List<String> names = new ArrayList<>();
        Map<String, Object> defaults = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : spec.entrySet()) {
            String attrName = entry.getKey();
            if (attrName.contains(":")) {
                String[] idxParts = Strings.partition(attrName, ":");
                attrName = idxParts[2];
                setAt(names, idxParts[0].equals("@") ? names.size() : Strings.toInt(idxParts[0]), attrName);
            }
            if (entry.getValue() != null && !Boolean.FALSE.equals(entry.getValue())) {
                defaults.put(attrName, entry.getValue());
            }
        }
        names.removeIf(java.util.Objects::isNull);
        option("positional_attrs", names);
        option("default_attrs", defaults);
        return this;
    }

    /**
     * Resolves all attributes (i.e., no positional attributes and no default attributes) (DSL).
     */
    public SyntaxProcessor resolveAttributes(boolean value) {
        option("positional_attrs", new ArrayList<String>());
        option("default_attrs", new LinkedHashMap<String, Object>());
        return this;
    }

    private static void setAt(List<String> names, int idx, String value) {
        while (names.size() <= idx) {
            names.add(null);
        }
        names.set(idx, value);
    }
}
