package org.asciidoctor.ast;

import java.util.List;
import java.util.Map;

public interface StructuralNode extends ContentNode {

    /**
     * Constant for special character replacement substitution like {@code <} to {@code &amp;lt;}.
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/special-characters/">Special Character Substitutions</a>
     */
    String SUBSTITUTION_SPECIAL_CHARACTERS = "specialcharacters";

    /**
     * Constant for quote replacements like {@code *bold*} to <b>{@code bold}</b>.
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/quotes/">Quotes Substitutions</a>
     */
    String SUBSTITUTION_QUOTES             = "quotes";

    /**
     * Constant for attribute replacements like {@code {foo}}.
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/attributes/">Attribute References Substitution</a>
     */
    String SUBSTITUTION_ATTRIBUTES         = "attributes";

    /**
     * Constant for replacements like {@code (C)} to {@code &#169;}.
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/replacements/">Character Replacement Substitutions</a>
     */
    String SUBSTITUTION_REPLACEMENTS       = "replacements";

    /**
     * Constant for macro replacements like {@code mymacro:target[]}.
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/macros/">Macro Substitutions</a>
     */
    String SUBSTITUTION_MACROS             = "macros";

    /**
     * Constant for post replacements like creating line breaks from a trailing {@code +} in a line.
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/post-replacements/">Post Replacement Substitutions</a>
     */
    String SUBSTITUTION_POST_REPLACEMENTS  = "post_replacements";

    String getTitle();
    void setTitle(String title);

    String getCaption();
    void setCaption(String caption);

    String getStyle();

    void setStyle(String style);

    /**
     * @return The list of child blocks of this block
     */
    List<StructuralNode> getBlocks();

    /**
     * Appends a new child block as the last block to this block.
     * @param block The new child block added as last child to this block.
     */
    void append(StructuralNode block);

    Object getContent();
    String convert();
    List<StructuralNode> findBy(Map<Object, Object> selector);

    int getLevel();

    void setLevel(int level);

    /**
     * Returns the content model.
     *
     * @see ContentModel
     *
     * @return the content model
     */
    String getContentModel();

    /**
     * Returns the source location of this block.
     * This information is only available if the {@code sourcemap} option is enabled when loading or rendering the document.
     * @return the source location of this block or {@code null} if the {@code sourcemap} option is not enabled when loading the document.
     */
    Cursor getSourceLocation();

    /**
     * Returns the list of enabled substitutions.
     * @return A list of substitutions enabled for this node, e.g. <code>["specialcharacters", "quotes", "attributes", "replacements", "macros", "post_replacements"]</code> for paragraphs.
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/">Asciidoctor User Manual</a>
     */
    List<String> getSubstitutions();

    /**
     * @param substitution the name of a substitution, e.g. {@link #SUBSTITUTION_POST_REPLACEMENTS}
     * @return <code>true</code> if the name of the given substitution is enabled.
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/">Asciidoctor User Manual</a>
     */
    boolean isSubstitutionEnabled(String substitution);

    /**
     * Removes the given substitution from this node.
     * @param substitution the name of a substitution, e.g. {@link #SUBSTITUTION_QUOTES}
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/">Substitutions</a>
     */
    void removeSubstitution(String substitution);

    /**
     * Adds the given substitution to this node at the end of the substitution list.
     * @param substitution the name of a substitution, e.g. {@link #SUBSTITUTION_MACROS}
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/">Substitutions</a>
     */
    void addSubstitution(String substitution);

    /**
     * Adds the given substitution to this node at the beginning of the substitution list.
     * @param substitution the name of a substitution, e.g. {@link #SUBSTITUTION_ATTRIBUTES}
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/">Substitutions</a>
     */
    void prependSubstitution(String substitution);

    /**
     * Sets the given substitutions on this node overwriting all other substitutions.
     * @param substitution the name of a substitution, e.g. {@link #SUBSTITUTION_SPECIAL_CHARACTERS}
     * @see <a href="https://docs.asciidoctor.org/asciidoc/latest/subs/">Substitutions</a>
     */
    void setSubstitutions(String... substitution);

}
