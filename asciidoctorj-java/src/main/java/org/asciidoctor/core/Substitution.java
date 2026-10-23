package org.asciidoctor.core;

/**
 * The substitutions that can be applied to text.
 */
public enum Substitution {
    SPECIAL_CHARACTERS("specialcharacters"),
    QUOTES("quotes"),
    ATTRIBUTES("attributes"),
    REPLACEMENTS("replacements"),
    MACROS("macros"),
    POST_REPLACEMENTS("post_replacements"),
    CALLOUTS("callouts"),
    HIGHLIGHT("highlight");

    private final String key;

    Substitution(String key) {
        this.key = key;
    }

    /**
     * @return the name of this substitution as used in the subs attribute (e.g., specialcharacters)
     */
    public String getKey() {
        return key;
    }

    /**
     * Resolves the substitution for the specified key.
     *
     * @param key the name of the substitution (e.g., post_replacements)
     * @return the matching substitution or null if not found
     */
    public static Substitution forKey(String key) {
        for (Substitution sub : values()) {
            if (sub.key.equals(key)) {
                return sub;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return key;
    }
}
