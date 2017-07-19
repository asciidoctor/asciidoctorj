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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by robertpanzer on 21.07.17.
 */
public class ExtensionGroupImpl implements ExtensionGroup {

  private final Ruby rubyRuntime;

  private final AsciidoctorModule asciidoctorModule;

  private final String groupName;

  private List<Object> docinfoProcessors = new ArrayList<>();
  private List<Object> preprocessors = new ArrayList<>();
  private List<Object> postprocessors = new ArrayList<>();
  private List<Object> includeProcessors = new ArrayList<>();
  private List<Object> treeProcessors = new ArrayList<>();
  private Map<String, Object> blockprocessors = new HashMap<>();
  private Map<String, Object> blockMacros = new HashMap<>();
  private Map<String, Object> inlineMacros = new HashMap<>();
  private List<String> requiredRubyLibraries = new ArrayList<>();

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
    asciidoctorModule.register_extension_group(this);
  }

  public List<Object> getDocinfoProcessors() {
    return docinfoProcessors;
  }

  public List<Object> getPreprocessors() {
    return preprocessors;
  }

  public List<Object> getPostprocessors() {
    return postprocessors;
  }

  public List<Object> getIncludeProcessors() {
    return includeProcessors;
  }

  public List<Object> getTreeProcessors() {
    return treeProcessors;
  }

  public Map<String, Object> getBlockprocessors() {
    return blockprocessors;
  }

  public Map<String, Object> getBlockMacros() {
    return blockMacros;
  }

  public Map<String, Object> getInlineMacros() {
    return inlineMacros;
  }

  public List<String> getRequiredRubyLibraries() {
    return requiredRubyLibraries;
  }

  @Override
  public void unregister() {
    asciidoctorModule.unregister_extension(groupName);
  }

  @Override
  public ExtensionGroup docinfoProcessor(final Class<? extends DocinfoProcessor> docInfoProcessor) {
    RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
    docinfoProcessors.add(rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup docinfoProcessor(final DocinfoProcessor docInfoProcessor) {
    RubyClass rubyClass = DocinfoProcessorProxy.register(rubyRuntime, docInfoProcessor);
    docinfoProcessors.add(rubyClass);
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
    RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
    preprocessors.add(rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup preprocessor(final Preprocessor preprocessor) {
    RubyClass rubyClass = PreprocessorProxy.register(rubyRuntime, preprocessor);
    preprocessors.add(rubyClass);
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
    RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
    postprocessors.add(rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup postprocessor(final Postprocessor postprocessor) {
    RubyClass rubyClass = PostprocessorProxy.register(rubyRuntime, postprocessor);
    postprocessors.add(rubyClass);
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
    RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
    includeProcessors.add(rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup includeProcessor(final IncludeProcessor includeProcessor) {
    RubyClass rubyClass = IncludeProcessorProxy.register(rubyRuntime, includeProcessor);
    includeProcessors.add(rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup treeprocessor(final Treeprocessor treeprocessor) {
    RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, treeprocessor);
    treeProcessors.add(rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup treeprocessor(final Class<? extends Treeprocessor> treeProcessor) {
    RubyClass rubyClass = TreeprocessorProxy.register(rubyRuntime, treeProcessor);
    treeProcessors.add(rubyClass);
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
    RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
    blockprocessors.put(blockName, rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup block(final Class<? extends BlockProcessor> blockProcessor) {
    RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
    blockprocessors.put(BlockProcessorProxy.getName(blockProcessor), rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup block(final BlockProcessor blockProcessor) {
    RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
    blockprocessors.put(blockProcessor.getName(), rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup block(final String blockName, final BlockProcessor blockProcessor) {
    RubyClass rubyClass = BlockProcessorProxy.register(rubyRuntime, blockProcessor);
    blockprocessors.put(blockName, rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup blockMacro(final String blockName, final Class<? extends BlockMacroProcessor> blockMacroProcessor) {
    RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
    blockMacros.put(blockName, rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup blockMacro(final Class<? extends BlockMacroProcessor> blockMacroProcessor) {
    RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
    blockMacros.put(AbstractProcessorProxy.getName(blockMacroProcessor), rubyClass);
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
    RubyClass rubyClass = BlockMacroProcessorProxy.register(rubyRuntime, blockMacroProcessor);
    blockMacros.put(blockMacroProcessor.getName(), rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final InlineMacroProcessor inlineMacroProcessor) {
    RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
    inlineMacros.put(inlineMacroProcessor.getName(), rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final String name, final Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
    RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
    inlineMacros.put(name, rubyClass);
    return this;
  }

  @Override
  public ExtensionGroup inlineMacro(final Class<? extends InlineMacroProcessor> inlineMacroProcessor) {
    RubyClass rubyClass = InlineMacroProcessorProxy.register(rubyRuntime, inlineMacroProcessor);
    inlineMacros.put(AbstractProcessorProxy.getName(inlineMacroProcessor), rubyClass);
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
    requiredRubyLibraries.add(requiredLibrary);
    return this;
  }

  @Override
  public ExtensionGroup loadRubyClass(final InputStream rubyClassStream) {
    RubyUtils.loadRubyClass(rubyRuntime, rubyClassStream);
    return this;
  }

  @Override
  public ExtensionGroup rubyPreprocessor(final String preprocessor) {
    preprocessors.add(preprocessor);
    return this;
  }

  @Override
  public ExtensionGroup rubyPostprocessor(final String postprocessor) {
    postprocessors.add(postprocessor);
    return this;
  }

  @Override
  public ExtensionGroup rubyDocinfoProcessor(final String docinfoProcessor) {
    docinfoProcessors.add(docinfoProcessor);
    return this;
  }

  @Override
  public ExtensionGroup rubyIncludeProcessor(final String includeProcessor) {
    includeProcessors.add(includeProcessor);
    return this;
  }

  @Override
  public ExtensionGroup rubyTreeprocessor(final String treeProcessor) {
    treeProcessors.add(treeProcessor);
    return this;
  }

  @Override
  public ExtensionGroup rubyBlock(final String blockName, final String blockProcessor) {
    blockprocessors.put(blockName, blockProcessor);
    return this;
  }

  @Override
  public ExtensionGroup rubyBlockMacro(final String blockName, final String blockMacroProcessor) {
    blockMacros.put(blockName, blockMacroProcessor);
    return this;
  }

  @Override
  public ExtensionGroup rubyInlineMacro(final String macroName, final String inlineMacroProcessor) {
    inlineMacros.put(macroName, inlineMacroProcessor);
    return this;
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
