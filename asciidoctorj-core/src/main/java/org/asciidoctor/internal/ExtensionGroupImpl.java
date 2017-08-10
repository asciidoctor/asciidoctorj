package org.asciidoctor.internal;

import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.ExtensionGroup;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.extension.processorproxies.AbstractProcessorProxy;
import org.asciidoctor.extension.processorproxies.BlockMacroProcessorProxy;
import org.asciidoctor.extension.processorproxies.BlockProcessorProxy;
import org.asciidoctor.extension.processorproxies.DocinfoProcessorProxy;
import org.asciidoctor.extension.processorproxies.IncludeProcessorProxy;
import org.asciidoctor.extension.processorproxies.InlineMacroProcessorProxy;
import org.asciidoctor.extension.processorproxies.PostprocessorProxy;
import org.asciidoctor.extension.processorproxies.PreprocessorProxy;
import org.asciidoctor.extension.processorproxies.TreeprocessorProxy;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubySymbol;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertpanzer on 21.07.17.
 */
public class ExtensionGroupImpl implements ExtensionGroup {

  private final Ruby rubyRuntime;

  private final AsciidoctorModule asciidoctorModule;

  private final String groupName;

  private List<Registrator> registrators = new ArrayList<>();

  public ExtensionGroupImpl(String groupName, JRubyAsciidoctor asciidoctor) {
    this.groupName = groupName;
    this.rubyRuntime = asciidoctor.getRubyRuntime();
    asciidoctorModule = asciidoctor.getAsciidoctorModule();
  }

  public String getGroupName() {
    return groupName;
  }

  @Override
  public void register() {
    asciidoctorModule.register_extension_group(this.groupName, this);
  }

  public void registerExtensions(Registry registry) {
    for (Registrator registrator: registrators) {
      registrator.register(registry);
    }
  }

  @Override
  public void unregister() {
    asciidoctorModule.unregister_extension(groupName);
  }

  @Override
  public ExtensionGroup docinfoProcessor(final Class<? extends DocinfoProcessor> docInfoProcessor) {
    final RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.docinfo_processor(rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup docinfoProcessor(final DocinfoProcessor docInfoProcessor) {
    final RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.docinfo_processor(rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup docinfoProcessor(final String docInfoProcessor) {
    try {
      final Class<? extends DocinfoProcessor>  docinfoProcessorClass = (Class<? extends DocinfoProcessor>) Class.forName(docInfoProcessor);
      docinfoProcessor(docinfoProcessorClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  @Override
  public ExtensionGroup preprocessor(final Class<? extends Preprocessor> preprocessor) {
    final RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.preprocessor(rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup preprocessor(final Preprocessor preprocessor) {
    final RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.preprocessor(rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup preprocessor(final String preprocessor) {
    try {
      final Class<? extends Preprocessor>  preprocessorClass = (Class<? extends Preprocessor>) Class.forName(preprocessor);
      preprocessor(preprocessorClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  @Override
  public ExtensionGroup postprocessor(final String postprocessor) {
    try {
        Class<? extends Postprocessor>  postprocessorClass = (Class<? extends Postprocessor>) Class.forName(postprocessor);
        postprocessor(postprocessorClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  @Override
  public ExtensionGroup postprocessor(final Class<? extends Postprocessor> postprocessor) {
    final RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.postprocessor(rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup postprocessor(final Postprocessor postprocessor) {
    final RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.postprocessor(rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup includeProcessor(final String includeProcessor) {
    try {
      Class<? extends IncludeProcessor>  includeProcessorClass = (Class<? extends IncludeProcessor>) Class.forName(includeProcessor);
      includeProcessor(includeProcessorClass);
      return this;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ExtensionGroup includeProcessor(final Class<? extends IncludeProcessor> includeProcessor) {
    final RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.include_processor(rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup includeProcessor(final IncludeProcessor includeProcessor) {
    final RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.include_processor(rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup treeprocessor(final Treeprocessor treeprocessor) {
    final RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, treeprocessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.tree_processor(rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup treeprocessor(final Class<? extends Treeprocessor> treeProcessor) {
    final RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, treeProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.tree_processor(rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup treeprocessor(final String treeProcessor) {
    try {
      Class<? extends Treeprocessor>  treeProcessorClass = (Class<? extends Treeprocessor>) Class.forName(treeProcessor);
      treeprocessor(treeProcessorClass);
      return this;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ExtensionGroup block(final String blockName, final String blockProcessor) {
    try {
      Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
      return block(blockName, blockProcessorClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ExtensionGroup block(final String blockProcessor) {
    try {
      Class<? extends BlockProcessor>  blockProcessorClass = (Class<? extends BlockProcessor>) Class.forName(blockProcessor);
      return block(blockProcessorClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ExtensionGroup block(final String blockName, final Class<? extends BlockProcessor> blockProcessor) {
    final RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.block(rubyClass, rubyRuntime.newSymbol(blockName));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup block(final Class<? extends BlockProcessor> blockProcessor) {
    final RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.block(rubyClass, rubyRuntime.newSymbol(BlockProcessorProxy.getName(blockProcessor)));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup block(final BlockProcessor blockProcessor) {
    final RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.block(rubyClass, rubyRuntime.newSymbol(blockProcessor.getName()));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup block(final String blockName, final BlockProcessor blockProcessor) {
    final RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.block(rubyClass, rubyRuntime.newSymbol(blockName));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup blockMacro(final String blockName, final Class<? extends BlockMacroProcessor> blockMacroProcessor) {
    final RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.block_macro(rubyClass, rubyRuntime.newSymbol(blockName));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup blockMacro(final Class<? extends BlockMacroProcessor> blockMacroProcessor) {
    final RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.block_macro(rubyClass, rubyRuntime.newSymbol(AbstractProcessorProxy.getName(blockMacroProcessor)));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup blockMacro(final String blockName, final String blockMacroProcessor) {
    try {
      Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
      return blockMacro(blockName, blockMacroProcessorClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ExtensionGroup blockMacro(final String blockMacroProcessor) {
    try {
      Class<? extends BlockMacroProcessor>  blockMacroProcessorClass = (Class<? extends BlockMacroProcessor>) Class.forName(blockMacroProcessor);
      return blockMacro(blockMacroProcessorClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ExtensionGroup blockMacro(final BlockMacroProcessor blockMacroProcessor) {
    final RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.block_macro(rubyClass, rubyRuntime.newSymbol(blockMacroProcessor.getName()));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final InlineMacroProcessor inlineMacroProcessor) {
    final RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.inline_macro(rubyClass, rubyRuntime.newSymbol(inlineMacroProcessor.getName()));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final String name, final Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
    final RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.inline_macro(rubyClass, rubyRuntime.newSymbol(name));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
    final RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.inline_macro(rubyClass, rubyRuntime.newSymbol(AbstractProcessorProxy.getName(inlineMacroProcessor)));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final String name, final String inlineMacroProcessor) {
    try {
      Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
      return inlineMacro(name, inlineMacroProcessorClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ExtensionGroup inlineMacro(final String inlineMacroProcessor) {
    try {
      Class<? extends InlineMacroProcessor>  inlineMacroProcessorClass = (Class<? extends InlineMacroProcessor>) Class.forName(inlineMacroProcessor);
      return inlineMacro(inlineMacroProcessorClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ExtensionGroup requireRubyLibrary(final String requiredLibrary) {
    RubyUtils.requireLibrary(rubyRuntime, requiredLibrary);
    return this;
  }

  @Override
  public ExtensionGroup loadRubyClass(final InputStream rubyClassStream) {
    RubyUtils.loadRubyClass(rubyRuntime, rubyClassStream);
    return this;
  }

  @Override
  public ExtensionGroup rubyPreprocessor(final String preprocessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.preprocessor(preprocessor);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyPostprocessor(final String postprocessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.postprocessor(postprocessor);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyDocinfoProcessor(final String docinfoProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.docinfo_processor(docinfoProcessor);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyIncludeProcessor(final String includeProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.include_processor(includeProcessor);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyTreeprocessor(final String treeProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.tree_processor(treeProcessor);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyBlock(final String blockName, final String blockProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.block(blockProcessor, rubyRuntime.newSymbol(blockName));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyBlockMacro(final String blockName, final String blockMacroProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.block_macro(blockMacroProcessor, rubyRuntime.newSymbol(blockName));
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyInlineMacro(final String macroName, final String inlineMacroProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(Registry registry) {
        registry.inline_macro(inlineMacroProcessor, rubyRuntime.newSymbol(macroName));
      }
    });
    return this;
  }

  public interface Registry {

    void docinfo_processor(RubyClass rubyClass);
    void docinfo_processor(String className);

    void preprocessor(RubyClass rubyClass);
    void preprocessor(String className);

    void postprocessor(RubyClass rubyClass);
    void postprocessor(String className);

    void include_processor(RubyClass rubyClass);
    void include_processor(String className);

    void tree_processor(RubyClass rubyClass);
    void tree_processor(String className);

    void block(RubyClass rubyClass, RubySymbol name);
    void block(String className, RubySymbol name);

    void block_macro(RubyClass rubyClass, RubySymbol name);
    void block_macro(String className, RubySymbol name);

    void inline_macro(RubyClass rubyClass, RubySymbol name);
    void inline_macro(String className, RubySymbol name);
  }

  public interface Registrator {
    void register(Registry registry);
  }
}
