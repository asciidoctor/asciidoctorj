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
import org.jruby.Ruby;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertpanzer on 21.07.17.
 */
public class ExtensionGroupImpl implements ExtensionGroup {

  private final JRubyAsciidoctor asciidoctor;

  private final Ruby rubyRuntime;

  private final AsciidoctorModule asciidoctorModule;

  private final String groupName;

  private final List<Runnable> registrations = new ArrayList<Runnable>();

  public ExtensionGroupImpl(String groupName, JRubyAsciidoctor asciidoctor) {
    this.groupName = groupName;
    this.asciidoctor = asciidoctor;
    this.rubyRuntime = asciidoctor.getRubyRuntime();
    asciidoctorModule = asciidoctor.getAsciidoctorModule();
  }

  @Override
  public void register() {
    for (Runnable r: registrations) {
      r.run();
    }
  }

  @Override
  public void unregister() {
    asciidoctorModule.unregister_extension(groupName);
  }

  @Override
  public ExtensionGroup docinfoProcessor(final Class<? extends DocinfoProcessor> docInfoProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, docInfoProcessor);
        asciidoctorModule.docinfo_processor(RubyUtils.toRubyClass(rubyRuntime, docInfoProcessor), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup docinfoProcessor(final DocinfoProcessor docInfoProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, docInfoProcessor.getClass());
        asciidoctorModule.docinfo_processor(docInfoProcessor, groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup docinfoProcessor(final String docInfoProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, docInfoProcessor);
        asciidoctorModule.docinfo_processor(getClassName(docInfoProcessor), groupName);

      }
    });
    return this;
  }

  @Override
  public ExtensionGroup preprocessor(final Class<? extends Preprocessor> preprocessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, preprocessor);
        asciidoctorModule.preprocessor(RubyUtils.toRubyClass(rubyRuntime, preprocessor), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup preprocessor(final Preprocessor preprocessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, preprocessor.getClass());
        asciidoctorModule.preprocessor(preprocessor, groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup preprocessor(final String preprocessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, preprocessor);
        asciidoctorModule.preprocessor(getClassName(preprocessor), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup postprocessor(final String postprocessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, postprocessor);
        asciidoctorModule.postprocessor(getClassName(postprocessor), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup postprocessor(final Class<? extends Postprocessor> postprocessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, postprocessor);
        asciidoctorModule.postprocessor(RubyUtils.toRubyClass(rubyRuntime, postprocessor), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup postprocessor(final Postprocessor postprocesor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, postprocesor.getClass());
        asciidoctorModule.postprocessor(postprocesor, groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup includeProcessor(final String includeProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, includeProcessor);
        asciidoctorModule.include_processor(getClassName(includeProcessor), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup includeProcessor(final Class<? extends IncludeProcessor> includeProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, includeProcessor);
        asciidoctorModule.include_processor(RubyUtils.toRubyClass(rubyRuntime, includeProcessor), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup includeProcessor(final IncludeProcessor includeProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        String importLine = getImportLine(includeProcessor.getClass());
        javaImport(rubyRuntime, importLine);
        asciidoctorModule.include_processor(includeProcessor, groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup treeprocessor(final Treeprocessor treeprocessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, treeprocessor.getClass());
        asciidoctorModule.treeprocessor(treeprocessor, groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup treeprocessor(final Class<? extends Treeprocessor> treeProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, treeProcessor);
        asciidoctorModule.treeprocessor(RubyUtils.toRubyClass(rubyRuntime, treeProcessor), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup treeprocessor(final String treeProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, treeProcessor);
        asciidoctorModule.treeprocessor(getClassName(treeProcessor), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup block(final String blockName, final String blockProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, blockProcessor);

        asciidoctorModule.block_processor(
            getClassName(blockProcessor),
            RubyUtils.toSymbol(rubyRuntime, blockName), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup block(final String blockName, final Class<? extends BlockProcessor> blockProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, blockProcessor);

        asciidoctorModule.block_processor(
            RubyUtils.toRubyClass(rubyRuntime, blockProcessor),
            RubyUtils.toSymbol(rubyRuntime, blockName), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup block(final BlockProcessor blockProcessor) {
    block(blockProcessor.getName(), blockProcessor);
    return this;
  }

  @Override
  public ExtensionGroup block(final String blockName, final BlockProcessor blockProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, blockProcessor.getClass());

        asciidoctorModule.block_processor(
            blockProcessor,
            RubyUtils.toSymbol(rubyRuntime, blockName), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup blockMacro(final String blockName, final Class<? extends BlockMacroProcessor> blockMacroProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, blockMacroProcessor);
        asciidoctorModule.block_macro(
            blockMacroProcessor.getSimpleName(),
            RubyUtils.toSymbol(rubyRuntime, blockName), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup blockMacro(final String blockName, final String blockMacroProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, blockMacroProcessor);
        asciidoctorModule.block_macro(
            getClassName(blockMacroProcessor),
            RubyUtils.toSymbol(rubyRuntime, blockName), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup blockMacro(final BlockMacroProcessor blockMacroProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, blockMacroProcessor.getClass());
        asciidoctorModule.block_macro(
            blockMacroProcessor,
            RubyUtils.toSymbol(rubyRuntime, blockMacroProcessor.getName()), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final InlineMacroProcessor inlineMacroProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, inlineMacroProcessor.getClass());

        asciidoctorModule.inline_macro(
            inlineMacroProcessor,
            RubyUtils.toSymbol(rubyRuntime, inlineMacroProcessor.getName()), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final String blockName, final Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, inlineMacroProcessor);

        asciidoctorModule.inline_macro(
            RubyUtils.toRubyClass(rubyRuntime, inlineMacroProcessor),
            RubyUtils.toSymbol(rubyRuntime, blockName), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final String blockName, final String inlineMacroProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        javaImport(rubyRuntime, inlineMacroProcessor);

        asciidoctorModule.inline_macro(
            getClassName(inlineMacroProcessor),
            RubyUtils.toSymbol(rubyRuntime, blockName), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup requireRubyLibrary(final String requiredLibrary) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        RubyUtils.requireLibrary(rubyRuntime, requiredLibrary);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup loadRubyClass(final InputStream rubyClassStream) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        RubyUtils.loadRubyClass(rubyRuntime, rubyClassStream);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyPreprocessor(final String preprocessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        asciidoctorModule.preprocessor(preprocessor, groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyPostprocessor(final String postprocessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        asciidoctorModule.postprocessor(postprocessor, groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyDocinfoProcessor(final String docinfoProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        asciidoctorModule.docinfo_processor(docinfoProcessor, groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyIncludeProcessor(final String includeProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        asciidoctorModule.include_processor(includeProcessor, groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyTreeprocessor(final String treeProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        asciidoctorModule.treeprocessor(treeProcessor, groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyBlock(final String blockName, final String blockProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        asciidoctorModule.block_processor(
            blockProcessor, RubyUtils.toSymbol(rubyRuntime, blockName), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyBlockMacro(final String blockName, final String blockMacroProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        asciidoctorModule.block_macro(
            blockMacroProcessor, RubyUtils.toSymbol(rubyRuntime, blockName), groupName);
      }
    });
    return this;
  }

  @Override
  public ExtensionGroup rubyInlineMacro(final String blockName, final String inlineMacroProcessor) {
    registrations.add(new Runnable() {
      @Override
      public void run() {
        asciidoctorModule.inline_macro(
            inlineMacroProcessor, RubyUtils.toSymbol(rubyRuntime, blockName), groupName);
      }
    });
    return this;
  }

  private void javaImport(Ruby ruby, Class<?> clazz) {
    ruby.evalScriptlet(String.format("java_import '%s'", getImportLine(clazz)));
  }

  private void javaImport(Ruby ruby, String className) {
    ruby.evalScriptlet(String.format("java_import '%s'", className));
  }

  private String getImportLine(Class<?> extensionClass) {
    int dollarPosition = -1;
    String className = extensionClass.getName();
    if ((dollarPosition = className.indexOf("$")) != -1) {
      className = className.substring(0, dollarPosition);
    }
    return className;
  }

  private String getClassName(String clazz) {
    return clazz.substring(clazz.lastIndexOf(".")+1);
  }

}
