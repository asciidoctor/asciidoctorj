package org.asciidoctor.internal;

import java.util.Map;

import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.converter.Converter;
import org.asciidoctor.extension.*;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyHash;


public interface AsciidoctorModule {

    void preprocessor(String preprocessorClassName);
    void preprocessor(String preprocessorClassName, String registrationName);
    void preprocessor(RubyClass preprocessorClassName);
    void preprocessor(RubyClass preprocessorClassName, String registrationName);
	  void preprocessor(Preprocessor preprocessor);
	  void preprocessor(Preprocessor preprocessor, String registrationName);

    void postprocessor(String postprocessorClassName);
    void postprocessor(String postprocessorClassName, String registrationName);
    void postprocessor(RubyClass postprocessorClassName);
    void postprocessor(RubyClass postprocessorClassName, String registrationName);
    void postprocessor(Postprocessor postprocessor);
    void postprocessor(Postprocessor postprocessor, String registrationName);

    void treeprocessor(String treeprocessor);
    void treeprocessor(String treeprocessor, String registrationName);
    void treeprocessor(RubyClass treeprocessorClassName);
    void treeprocessor(RubyClass treeprocessorClassName, String registrationName);
    void treeprocessor(Treeprocessor treeprocessorClassName);
    void treeprocessor(Treeprocessor treeprocessorClassName, String registrationName);

    void include_processor(String includeProcessorClassName);
    void include_processor(String includeProcessorClassName, String registrationName);
    void include_processor(RubyClass includeProcessorClassName);
    void include_processor(RubyClass includeProcessorClassName, String registrationName);
    void include_processor(IncludeProcessor includeProcessor);
    void include_processor(IncludeProcessor includeProcessor, String registrationName);

    void block_processor(String blockClassName, Object blockName);
    void block_processor(String blockClassName, Object blockName, String registrationName);
    void block_processor(RubyClass blockClass, Object blockName);
    void block_processor(RubyClass blockClass, Object blockName, String registrationName);
    void block_processor(BlockProcessor blockInstance, Object blockName);
    void block_processor(BlockProcessor blockInstance, Object blockName, String registrationName);

    void block_macro(String blockMacroClassName, Object blockName);
    void block_macro(String blockMacroClassName, Object blockName, String registrationName);
    void block_macro(Class<BlockMacroProcessor> blockMacroClass, Object blockName);
    void block_macro(Class<BlockMacroProcessor> blockMacroClass, Object blockName, String registrationName);
    void block_macro(BlockMacroProcessor blockMacroInstance, Object blockName);
    void block_macro(BlockMacroProcessor blockMacroInstance, Object blockName, String registrationName);

    void inline_macro(String blockClassName, Object blockSymbol);
    void inline_macro(String blockClassName, Object blockSymbol, String registrationName);
    void inline_macro(RubyClass blockClassName, Object blockSymbol);
    void inline_macro(RubyClass blockClassName, Object blockSymbol, String registrationName);
    void inline_macro(InlineMacroProcessor blockClassName, Object blockSymbol);
    void inline_macro(InlineMacroProcessor blockClassName, Object blockSymbol, String registrationName);

    void docinfo_processor(String docInfoClassName);
    void docinfo_processor(String docInfoClassName, String registrationName);
    void docinfo_processor(RubyClass docInfoClassName);
    void docinfo_processor(RubyClass docInfoClassName, String registrationName);
    void docinfo_processor(DocinfoProcessor docInfoClassName);
    void docinfo_processor(DocinfoProcessor docInfoClassName, String registrationName);

    void unregister_all_extensions();
    void unregister_extension(String registrationName);

    Object convert(String content, Map<String, Object> options);
    Object convertFile(String filename, Map<String, Object> options);

    DocumentRuby load_file(String filename, Map<String, Object> options);
    DocumentRuby load(String content, Map<String, Object> options);

    void register_converter(RubyClass converter);
    void register_converter(RubyClass converter, String[] backends);
    RubyClass resolve_converter(String backend);
    RubyArray converters();
    void unregister_all_converters();

	String asciidoctorRuntimeEnvironmentVersion();
	
}
