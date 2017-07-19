package org.asciidoctor.extension;


import java.io.InputStream;

/**
 * An ExtensionGroup allows to collectively register and unregister extensions.
 * All extensions are registered lazily and are not effective before a call to {@link #register()}.
 *
 * <p>Example:
 * <code><pre>
 * ExtensionGroup group = asciidoctor.createGroup();
 * group.block(myBlock).preprocessor(mypreprocessor);
 *
 * // Convert with extensions
 * group.register();
 * asciidoctor.convert(...);
 * group.unregister();
 *
 * // Convert without extensions
 * asciidoctor.convert(...);
 * </pre></code></p>
 */
public interface ExtensionGroup {

  public void register();

  public void unregister();

  public ExtensionGroup docinfoProcessor(Class<? extends DocinfoProcessor> docInfoProcessor);
  public ExtensionGroup docinfoProcessor(DocinfoProcessor docInfoProcessor);
  public ExtensionGroup docinfoProcessor(String docInfoProcessor);

  public ExtensionGroup preprocessor(Class<? extends Preprocessor> preprocessor);
  public ExtensionGroup preprocessor(Preprocessor preprocessor);
  public ExtensionGroup preprocessor(String preprocessor);

  public ExtensionGroup postprocessor(String postprocessor);
  public ExtensionGroup postprocessor(Class<? extends Postprocessor> postprocessor);
  public ExtensionGroup postprocessor(Postprocessor postprocesor);

  public ExtensionGroup includeProcessor(String includeProcessor);
  public ExtensionGroup includeProcessor(Class<? extends IncludeProcessor> includeProcessor);
  public ExtensionGroup includeProcessor(IncludeProcessor includeProcessor);

  public ExtensionGroup treeprocessor(Treeprocessor treeprocessor);
  public ExtensionGroup treeprocessor(Class<? extends Treeprocessor> treeProcessor);
  public ExtensionGroup treeprocessor(String treeProcessor);

  public ExtensionGroup block(String blockName, String blockProcessor);
  public ExtensionGroup block(String blockProcessor);
  public ExtensionGroup block(String blockName, Class<? extends BlockProcessor> blockProcessor);
  public ExtensionGroup block(Class<? extends BlockProcessor> blockProcessor);
  public ExtensionGroup block(String blockName, BlockProcessor blockProcessor);
  public ExtensionGroup block(BlockProcessor blockProcessor);

  public ExtensionGroup blockMacro(String blockName, Class<? extends BlockMacroProcessor> blockMacroProcessor);
  public ExtensionGroup blockMacro(Class<? extends BlockMacroProcessor> blockMacroProcessor);
  public ExtensionGroup blockMacro(String blockName, String blockMacroProcessor);
  public ExtensionGroup blockMacro(String blockMacroProcessor);
  public ExtensionGroup blockMacro(BlockMacroProcessor blockMacroProcessor);

  public ExtensionGroup inlineMacro(InlineMacroProcessor inlineMacroProcessor);
  public ExtensionGroup inlineMacro(String name, Class<? extends InlineMacroProcessor> inlineMacroProcessor);
  public ExtensionGroup inlineMacro(Class<? extends InlineMacroProcessor> inlineMacroProcessor);
  public ExtensionGroup inlineMacro(String name, String inlineMacroProcessor);
  public ExtensionGroup inlineMacro(String inlineMacroProcessor);

  public ExtensionGroup requireRubyLibrary(String requiredLibrary);
  public ExtensionGroup loadRubyClass(InputStream rubyClassStream);

  public ExtensionGroup rubyPreprocessor(String preprocessor);
  public ExtensionGroup rubyPostprocessor(String postprocessor);
  public ExtensionGroup rubyDocinfoProcessor(String docinfoProcessor);
  public ExtensionGroup rubyIncludeProcessor(String includeProcessor);
  public ExtensionGroup rubyTreeprocessor(String treeProcessor);
  public ExtensionGroup rubyBlock(String blockName, String blockProcessor);
  public ExtensionGroup rubyBlockMacro(String blockName, String blockMacroProcessor);
  public ExtensionGroup rubyInlineMacro(String blockName, String inlineMacroProcessor);
}
