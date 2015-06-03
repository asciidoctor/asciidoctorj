package org.asciidoctor.extension;

import org.asciidoctor.Options;
import org.asciidoctor.ast.*;
import org.asciidoctor.internal.RubyHashMapDecorator;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.java.proxies.RubyObjectHolderProxy;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Processor {

    /**
     * This value is used as the config option key to configure how Asciidoctor should treat blocks created by
     * this Processor.
     * Its value must be a String constant.
     *
     * <p>Example to Asciidoctor know that a BlockProcessor creates zero or more child blocks:
     * <pre>
     * <verbatim>
     * Map&lt;String, Object&gt; config = new HashMap&lt;&gt;();
     * config.put(CONTENT_MODEL, CONTENT_MODEL_COMPOUND);
     * BlockProcessor blockProcessor = new BlockProcessor("foo", config);
     * asciidoctor.javaExtensionRegistry().block(blockProcessor);
     * </verbatim>
     * </pre>
     * </p>
     */
    public static final String CONTENT_MODEL = "content_model";

    /**
     * Predefined constant to let Asciidoctor know that this BlockProcessor creates zero or more child blocks.
     */
    public static final String CONTENT_MODEL_COMPOUND = ":compound";

    /**
     * Predefined constant to let Asciidoctor know that this BlockProcessor creates simple paragraph content.
     */
    public static final String CONTENT_MODEL_SIMPLE =":simple";

    /**
     * Predefined constant to let Asciidoctor know that this BlockProcessor creates literal content.
     */
    public static final String CONTENT_MODEL_VERBATIM =":verbatim";

    /**
     * Predefined constant to make Asciidoctor pass through the content unprocessed.
     */
    public static final String CONTENT_MODEL_RAW =":raw";

    /**
     * Predefined constant to make Asciidoctor drop the content.
     */
    public static final String CONTENT_MODEL_SKIP =":skip";

    /**
     * Predefined constant to make Asciidoctor not expect any content.
     */
    public static final String CONTENT_MODEL_EMPTY =":empty";

    /**
     * Predefined constant to make Asciidoctor parse content as attributes.
     */
    public static final String CONTENT_MODEL_ATTRIBUTES =":attributes";


    protected Map<String, Object> config;

    protected Ruby rubyRuntime;

    /**
     * The config map must not be reset once configFinalized is true.
     * With the Asciidoctor Ruby implementation this flag will be set to
     * true after the Ruby part of the extension is initialized.
     */
    private boolean configFinalized = false;

    public Processor() {
        this(new HashMap<String, Object>());
    }

    public Processor(Map<String, Object> config) {
        this.config = new HashMap<String, Object>(config);
    }

    public Map<String, Object> getConfig() {
    	return this.config;
    }

    public final void setConfig(Map<String, Object> config) {
        if (configFinalized) {
            throw new IllegalStateException("It is only allowed to set the config in the constructor!");
        }
        this.config = config;
    }

    /**
     * Lock the config of this processor so that it is no longer allowed to invoke {@link #setConfig(Map)}.
     */
    public final void setConfigFinalized() {
        this.configFinalized = true;
    }

    public Block createBlock(AbstractBlock parent, String context, String content, Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<Object, Object>());
    }

    public Block createBlock(AbstractBlock parent, String context, String content, Map<String, Object> attributes,
            Map<Object, Object> options) {

        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, attributes);        
        
        return createBlock(parent, context, options);
    }

    public Block createBlock(AbstractBlock parent, String context, List<String> content, Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<Object, Object>());
    }

    public Block createBlock(AbstractBlock parent, String context, List<String> content, Map<String, Object> attributes,
            Map<Object, Object> options) {

        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, new HashMap<String, Object>(attributes));
        
        return createBlock(parent, context, options);
    }

    public Inline createInline(AbstractBlock parent, String context, List<String> text, Map<String, Object> attributes, Map<Object, Object> options) {

        Ruby rubyRuntime = getRubyRuntimeFromNode(parent);

        options.put(Options.ATTRIBUTES, attributes);
        
        IRubyObject rubyClass = rubyRuntime.evalScriptlet("Asciidoctor::Inline");
        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
                options);
        Object[] parameters = {
                ((AbstractBlockImpl) parent).getRubyObject(),
                RubyUtils.toSymbol(rubyRuntime, context),
                text,
                convertMapToRubyHashWithSymbols };
        return (Inline) NodeConverter.createASTNode(
                JavaEmbedUtils.invokeMethod(rubyRuntime, rubyClass, "new", parameters, Inline.class));
    }
    
    public Inline createInline(AbstractBlock parent, String context, String text, Map<String, Object> attributes, Map<String, Object> options) {
        
        options.put(Options.ATTRIBUTES, attributes);

        Ruby rubyRuntime = getRubyRuntimeFromNode(parent);

        IRubyObject rubyClass = rubyRuntime.evalScriptlet("Asciidoctor::Inline");
        RubyHash convertedOptions = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

        Object[] parameters = {
                ((AbstractBlockImpl) parent).getRubyObject(),
                RubyUtils.toSymbol(rubyRuntime, context),
                text,
                convertedOptions };
        return (Inline) NodeConverter.createASTNode(
                JavaEmbedUtils.invokeMethod(rubyRuntime, rubyClass, "new", parameters, Inline.class));
    }
    
    private Block createBlock(AbstractBlock parent, String context,
            Map<Object, Object> options) {

        Ruby rubyRuntime = getRubyRuntimeFromNode(parent);

        IRubyObject rubyClass = rubyRuntime.evalScriptlet("Asciidoctor::Block");
        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
                options);

        Object[] parameters = {
                ((AbstractBlockImpl) parent).getRubyObject(),
                RubyUtils.toSymbol(rubyRuntime, context),
                convertMapToRubyHashWithSymbols };
        return (Block) NodeConverter.createASTNode(JavaEmbedUtils.invokeMethod(rubyRuntime, rubyClass, "new", parameters, Block.class));
    }

    private Ruby getRubyRuntimeFromNode(AbstractNode node) {
        if (node instanceof IRubyObject) {
            return ((IRubyObject) node).getRuntime();
        } else if (node instanceof RubyObjectHolderProxy) {
            return ((RubyObjectHolderProxy) node).__ruby_object().getRuntime();
        } else if (node instanceof AbstractNodeImpl) {
            IRubyObject nodeDelegate = ((AbstractNodeImpl) node).getRubyObject();
            return nodeDelegate.getRuntime();
        } else {
            throw new IllegalArgumentException("Don't know what to with a " + node);
        }
    }

}
