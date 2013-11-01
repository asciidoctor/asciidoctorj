package org.asciidoctor.internal;

import java.util.List;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyObject;

public class BlockImpl implements Block{
	private Block blockRuby;
	private Ruby rubyRuntime;

	public BlockImpl(Block blockRuby, Ruby rubyRuntime) {
        this.blockRuby = blockRuby;
        this.rubyRuntime = rubyRuntime;
    }

	@Override
	public String id() {
		return blockRuby.id();
	}

	@Override
	public String title() {
		return blockRuby.title();
	}

	@Override
	public String role() {
		return blockRuby.role();
	}

	@Override
	public String style() {
		return blockRuby.style();
	}

	@Override
	public List<Block> blocks() {
		List rubyBlocks = this.blockRuby.blocks();

		for (int i = 0; i < rubyBlocks.size(); i++) {
			if (!(rubyBlocks.get(i) instanceof RubyArray) && !(rubyBlocks.get(i) instanceof Block)) {
				Block blockRuby = RubyUtils.rubyToJava(rubyRuntime,
						(RubyObject) rubyBlocks.get(i), Block.class);
				rubyBlocks.set(i, new BlockImpl(blockRuby, rubyRuntime));
			}
		}

		return rubyBlocks;
	}

	@Override
	public Map<String, Object> attributes() {
		return blockRuby.attributes();
	}

	@Override
	public Object content() {
		return blockRuby.content();
	}

	@Override
	public String render() {
		return blockRuby.render();
	}

	@Override
	public String context() {
		return blockRuby.context();
	}

	@Override
	public List<String> lines() {
		return blockRuby.lines();
	}
}
