package org.asciidoctor.jruby.internal;

import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.ExtensionGroup;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Postprocessor;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.jruby.extension.processorproxies.AbstractProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.BlockMacroProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.BlockProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.DocinfoProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.IncludeProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.InlineMacroProcessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.PostprocessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.PreprocessorProxy;
import org.asciidoctor.jruby.extension.processorproxies.TreeprocessorProxy;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.Block;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertpanzer on 21.07.17.
 */
public class ExtensionGroupImpl implements ExtensionGroup {

  private JRubyAsciidoctor asciidoctor;

  private final Ruby rubyRuntime;

  private final String groupName;

  private final RubyModule asciidoctorModule;
  private final RubyClass extensionGroupClass;

  private List<Registrator> registrators = new ArrayList<>();

  public ExtensionGroupImpl(String groupName, JRubyAsciidoctor asciidoctor, RubyClass extensionGroupClass) {
    this.groupName = groupName;
    this.asciidoctor = asciidoctor;
    this.rubyRuntime = asciidoctor.getRubyRuntime();
    this.asciidoctorModule = rubyRuntime.getModule("AsciidoctorModule");
    this.extensionGroupClass = extensionGroupClass;
  }

  public String getGroupName() {
    return groupName;
  }

  @Override
  public void register() {
    IRubyObject callback = extensionGroupClass.newInstance(rubyRuntime.getCurrentContext(), Block.NULL_BLOCK);
    asciidoctorModule.callMethod("register_extension_group",
        rubyRuntime.newString(this.groupName),
        callback,
        JavaEmbedUtils.javaToRuby(rubyRuntime, registrators));
  }

  static RubyClass createExtensionGroupClass(final Ruby rubyRuntime) {
    final RubyClass extensionGroupClass = rubyRuntime.getModule("AsciidoctorModule")
        .defineClassUnder("ExtensionGroupImpl", rubyRuntime.getObject(), new ObjectAllocator() {
          @Override
          public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
            return new ExtensionGroupRegistrationCallback(runtime, klazz);
          }
        });
    extensionGroupClass.defineAnnotatedMethods(ExtensionGroupRegistrationCallback.class);
    return extensionGroupClass;
  }

  public static class ExtensionGroupRegistrationCallback extends RubyObject {

    private List<Registrator> registrators = new ArrayList<>();

    ExtensionGroupRegistrationCallback(Ruby runtime, RubyClass metaClass) {
      super(runtime, metaClass);
    }

    @JRubyMethod(name = "register_extensions", required = 2)
    public IRubyObject registerExtensions(ThreadContext context, IRubyObject registry, IRubyObject rubyRegistrators) {
      List<Registrator> registrators = (List<Registrator>) JavaEmbedUtils.rubyToJava(rubyRegistrators);
      for (Registrator registrator: registrators) {
        registrator.register(registry);
      }
      return context.getRuntime().getNil();
    }
  }

  @Override
  public void unregister() {
    asciidoctorModule
        .callMethod("unregister_extension", rubyRuntime.newString(this.groupName));
  }

  @Override
  public ExtensionGroup docinfoProcessor(final Class<? extends DocinfoProcessor> docInfoProcessor) {
    final RubyClass rubyClass = DocinfoProcessorProxy.register(asciidoctor, docInfoProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "docinfo_processor", rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup docinfoProcessor(final DocinfoProcessor docInfoProcessor) {
    final RubyClass rubyClass = DocinfoProcessorProxy.register(asciidoctor, docInfoProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "docinfo_processor", rubyClass);
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
    final RubyClass rubyClass = PreprocessorProxy.register(asciidoctor, preprocessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "preprocessor", rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup preprocessor(final Preprocessor preprocessor) {
    final RubyClass rubyClass = PreprocessorProxy.register(asciidoctor, preprocessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "preprocessor", rubyClass);
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
    final RubyClass rubyClass = PostprocessorProxy.register(asciidoctor, postprocessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "postprocessor", rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup postprocessor(final Postprocessor postprocessor) {
    final RubyClass rubyClass = PostprocessorProxy.register(asciidoctor, postprocessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "postprocessor", rubyClass);
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
    final RubyClass rubyClass = IncludeProcessorProxy.register(asciidoctor, includeProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "include_processor", rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup includeProcessor(final IncludeProcessor includeProcessor) {
    final RubyClass rubyClass = IncludeProcessorProxy.register(asciidoctor, includeProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "include_processor", rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup treeprocessor(final Treeprocessor treeprocessor) {
    final RubyClass rubyClass = TreeprocessorProxy.register(asciidoctor, treeprocessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "tree_processor", rubyClass);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup treeprocessor(final Class<? extends Treeprocessor> treeProcessor) {
    final RubyClass rubyClass = TreeprocessorProxy.register(asciidoctor, treeProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "tree_processor", rubyClass);
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
    final RubyClass rubyClass = BlockProcessorProxy.register(asciidoctor, blockProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block", new IRubyObject[]{rubyClass, rubyRuntime.newSymbol(blockName)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup block(final Class<? extends BlockProcessor> blockProcessor) {
    final RubyClass rubyClass = BlockProcessorProxy.register(asciidoctor, blockProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block", new IRubyObject[]{rubyClass, rubyRuntime.newSymbol(BlockProcessorProxy.getName(blockProcessor))});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup block(final BlockProcessor blockProcessor) {
    final RubyClass rubyClass = BlockProcessorProxy.register(asciidoctor, blockProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block", new IRubyObject[]{rubyClass, rubyRuntime.newSymbol(blockProcessor.getName())});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup block(final String blockName, final BlockProcessor blockProcessor) {
    final RubyClass rubyClass = BlockProcessorProxy.register(asciidoctor, blockProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block", new IRubyObject[]{rubyClass, rubyRuntime.newSymbol(blockName)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup blockMacro(final String blockName, final Class<? extends BlockMacroProcessor> blockMacroProcessor) {
    final RubyClass rubyClass = BlockMacroProcessorProxy.register(asciidoctor, blockMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block_macro", new IRubyObject[]{rubyClass, rubyRuntime.newSymbol(blockName)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup blockMacro(final Class<? extends BlockMacroProcessor> blockMacroProcessor) {
    final RubyClass rubyClass = BlockMacroProcessorProxy.register(asciidoctor, blockMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block_macro", new IRubyObject[]{rubyClass, rubyRuntime.newSymbol(AbstractProcessorProxy.getName(blockMacroProcessor))});
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
    final RubyClass rubyClass = BlockMacroProcessorProxy.register(asciidoctor, blockMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block_macro", new IRubyObject[]{rubyClass, rubyRuntime.newSymbol(blockMacroProcessor.getName())});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final InlineMacroProcessor inlineMacroProcessor) {
    final RubyClass rubyClass = InlineMacroProcessorProxy.register(asciidoctor, inlineMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "inline_macro", new IRubyObject[]{rubyClass, rubyRuntime.newSymbol(inlineMacroProcessor.getName())});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final String name, final Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
    final RubyClass rubyClass = InlineMacroProcessorProxy.register(asciidoctor, inlineMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "inline_macro", new IRubyObject[]{rubyClass, rubyRuntime.newSymbol(name)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
    final RubyClass rubyClass = InlineMacroProcessorProxy.register(asciidoctor, inlineMacroProcessor);
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "inline_macro", new IRubyObject[]{rubyClass, rubyRuntime.newSymbol(AbstractProcessorProxy.getName(inlineMacroProcessor))});
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
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "preprocessor", new IRubyObject[]{rubyRuntime.newString(preprocessor)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyPostprocessor(final String postprocessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "postprocessor", new IRubyObject[]{rubyRuntime.newString(postprocessor)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyDocinfoProcessor(final String docinfoProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "docinfo_processor", new IRubyObject[]{rubyRuntime.newString(docinfoProcessor)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyIncludeProcessor(final String includeProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "include_processor", new IRubyObject[]{rubyRuntime.newString(includeProcessor)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyTreeprocessor(final String treeProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "tree_processor", new IRubyObject[]{rubyRuntime.newString(treeProcessor)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyBlock(final String blockName, final String blockProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block", new IRubyObject[]{rubyRuntime.newString(blockProcessor), rubyRuntime.newSymbol(blockName)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyBlock(final String blockProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block", new IRubyObject[]{rubyRuntime.newString(blockProcessor)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyBlockMacro(final String blockName, final String blockMacroProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block_macro", new IRubyObject[]{rubyRuntime.newString(blockMacroProcessor), rubyRuntime.newSymbol(blockName)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyBlockMacro(final String blockMacroProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "block_macro", new IRubyObject[]{rubyRuntime.newString(blockMacroProcessor)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyInlineMacro(final String macroName, final String inlineMacroProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "inline_macro", new IRubyObject[]{rubyRuntime.newString(inlineMacroProcessor), rubyRuntime.newSymbol(macroName)});
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyInlineMacro(final String inlineMacroProcessor) {
    registrators.add(new Registrator() {
      @Override
      public void register(IRubyObject registry) {
        registry.callMethod(rubyRuntime.getCurrentContext(), "inline_macro", new IRubyObject[]{rubyRuntime.newString(inlineMacroProcessor)});
      }
    });
    return this;
  }

  public interface Registrator {
    void register(IRubyObject registry);
  }
}
