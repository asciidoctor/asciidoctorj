package org.asciidoctor.extension;

import org.asciidoctor.Options;
import org.asciidoctor.ast.*;
import org.asciidoctor.internal.JRubyRuntimeContext;
import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyHash;
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


    protected RubyHash config;
    protected Ruby rubyRuntime;

    public Processor(Map<String, Object> config) {
        this.rubyRuntime = JRubyRuntimeContext.get();
        this.config = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, config);
    }

    public void update_config(Map<Object, Object> config) {
    	this.config.putAll(config);
    }
    
    public Map<Object, Object> getConfig() {
    	return this.config;
    }

    public RubyHash setConfig(Map<String, Object> config) {
        this.config = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, config);
        return null;
    }

    public Block createBlock(BlockNode parent, String context, String content, Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<Object, Object>());
    }

    public Block createBlock(BlockNode parent, String context, String content, Map<String, Object> attributes,
            Map<Object, Object> options) {

        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, attributes);        
        
        return createBlock(parent, context, options);
    }

    public Block createBlock(BlockNode parent, String context, List<String> content, Map<String, Object> attributes) {
        return createBlock(parent, context, content, attributes, new HashMap<Object, Object>());
    }

    public Block createBlock(BlockNode parent, String context, List<String> content, Map<String, Object> attributes,
            Map<Object, Object> options) {

        options.put(Options.SOURCE, content);
        options.put(Options.ATTRIBUTES, attributes);        
        
        return createBlock(parent, context, options);
    }

    public Inline createInline(BlockNode parent, String context, List<String> text, Map<String, Object> attributes, Map<Object, Object> options) {
        
        options.put(Options.ATTRIBUTES, attributes);
        
        IRubyObject rubyClass = rubyRuntime.evalScriptlet("Asciidoctor::Inline");
        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
                options);
        Object[] parameters = {
                parent.delegate(),
                RubyUtils.toSymbol(rubyRuntime, context),
                text,
                convertMapToRubyHashWithSymbols };
        return (Inline) JavaEmbedUtils.invokeMethod(rubyRuntime, rubyClass,
                "new", parameters, Inline.class);
    }
    
    public Inline createInline(BlockNode parent, String context, String text, Map<String, Object> attributes, Map<String, Object> options) {
        
        options.put(Options.ATTRIBUTES, attributes);
        
        IRubyObject rubyClass = rubyRuntime.evalScriptlet("Asciidoctor::Inline");
        RubyHash convertedOptions = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);
        // FIXME hack to ensure we have the underlying Ruby instance
        try {
            parent = parent.delegate();
        } catch (Exception e) {}

        Object[] parameters = {
                parent,
                RubyUtils.toSymbol(rubyRuntime, context),
                text,
                convertedOptions };
        return (Inline) JavaEmbedUtils.invokeMethod(rubyRuntime, rubyClass,
                "new", parameters, Inline.class);
    }
    
//    protected Document document(Document document) {
//    	return new Document(document, rubyRuntime);
//    }
    
    private Block createBlock(BlockNode parent, String context,
            Map<Object, Object> options) {
        IRubyObject rubyClass = rubyRuntime.evalScriptlet("Asciidoctor::Block");
        RubyHash convertMapToRubyHashWithSymbols = RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(rubyRuntime,
                options);
        // FIXME hack to ensure we have the underlying Ruby instance
        try {
            parent = parent.delegate();
        } catch (Exception e) {}

        Object[] parameters = {
                parent,
                RubyUtils.toSymbol(rubyRuntime, context),
                convertMapToRubyHashWithSymbols };
        return (Block) JavaEmbedUtils.invokeMethod(rubyRuntime, rubyClass,
                "new", parameters, Block.class);
    }

}
