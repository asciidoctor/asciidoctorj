package org.asciidoctor.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.asciidoctor.internal.RubyHashUtil;
import org.asciidoctor.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyObject;

public class AbstractBlockImpl implements AbstractBlock {
	
	protected AbstractBlock delegate;
	protected Ruby runtime;

	public AbstractBlockImpl(AbstractBlock blockDelegate, Ruby runtime) {
        this.delegate = blockDelegate;
        this.runtime = runtime;
    }
	
	@Override
	public String id() {
		return delegate.id();
	}

	@Override
	public String title() {
		return delegate.title();
	}

	@Override
	public String role() {
		return delegate.role();
	}

	@Override
	public String style() {
		return delegate.style();
	}

	@Override
	public List<Block> blocks() {
		List<Block> rubyBlocks = delegate.blocks();

		for (int i = 0; i < rubyBlocks.size(); i++) {
			if (!(rubyBlocks.get(i) instanceof RubyArray) && !(rubyBlocks.get(i) instanceof Block)) {
				Block blockRuby = RubyUtils.rubyToJava(runtime,
						(RubyObject) rubyBlocks.get(i), Block.class);
				rubyBlocks.set(i, new BlockImpl(blockRuby, runtime));
			}
		}

		return rubyBlocks;
	}

	@Override
	public Map<String, Object> attributes() {
		return delegate.attributes();
	}

	@Override
	public Object content() {
		return delegate.content();
	}

	@Override
	public String render() {
		return delegate.render();
	}

	@Override
	public String context() {
		return delegate.context();
	}

    @Override
    public AbstractBlock delegate() {
        return delegate;
    }

	@Override
	public DocumentRuby document() {
		return delegate.document();
	}

    @Override
    public List<AbstractBlock> findBy(Map<Object, Object> selector) {
        
        List<AbstractBlock> findBy = delegate.findBy(RubyHashUtil.convertMapToRubyHashWithSymbolsIfNecessary(runtime, selector));
        
        List<AbstractBlock> returnBlocks = new ArrayList<AbstractBlock>();
        for (AbstractBlock abstractBlock : findBy) {
            
            if (!(abstractBlock instanceof RubyArray) && !(abstractBlock instanceof AbstractBlock)) {
                
            }
            
        }
        
        return returnBlocks;
    }

}
