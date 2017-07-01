package org.asciidoctor.internal;

import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.*;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubySymbol;


public interface AsciidoctorModule {

    void preprocessor(RubySymbol name, String preprocessorClassName);
    void preprocessor(RubySymbol name, RubyClass preprocessorClassName);
    void preprocessor(RubySymbol name, Preprocessor preprocessor);
	
    void postprocessor(RubySymbol name, String postprocessorClassName);
    void postprocessor(RubySymbol name, RubyClass postprocessorClassName);
    void postprocessor(RubySymbol name, Postprocessor postprocessor);
    
    void treeprocessor(RubySymbol name, String treeprocessor);
    void treeprocessor(RubySymbol name, RubyClass treeprocessorClassName);
    void treeprocessor(RubySymbol name, Treeprocessor treeprocessorClassName);
    
    void include_processor(RubySymbol name, String includeProcessorClassName);
    void include_processor(RubySymbol name, RubyClass includeProcessorClassName);
    void include_processor(RubySymbol name, IncludeProcessor includeProcessor);
    
    void block_processor(RubySymbol name, String blockClassName, Object blockName);
    void block_processor(RubySymbol name, RubyClass blockClass, Object blockName);
    void block_processor(RubySymbol name, BlockProcessor blockInstance, Object blockName);
    
    void block_macro(RubySymbol name, String blockMacroClassName, Object blockName);
    void block_macro(RubySymbol name, Class<BlockMacroProcessor> blockMacroClass, Object blockName);
    void block_macro(RubySymbol name, BlockMacroProcessor blockMacroInstance, Object blockName);
    void block_macro(RubySymbol name, RubyClass blockClassName, Object blockSymbol);

    void inline_macro(RubySymbol name, String blockClassName, Object blockSymbol);
    void inline_macro(RubySymbol name, RubyClass blockClassName, Object blockSymbol);
    void inline_macro(RubySymbol name, InlineMacroProcessor blockClassName, Object blockSymbol);
    
    void docinfo_processor(RubySymbol name, String docInfoClassName);
    void docinfo_processor(RubySymbol name, RubyClass docInfoClassName);
    void docinfo_processor(RubySymbol name, DocinfoProcessor docInfoClassName);
    
    void unregister_all_extensions();
    void unregister_extension(RubySymbol name);

    Object convert(String content, Map<String, Object> options);
    Object convertFile(String filename, Map<String, Object> options);

    Document load_file(String filename, Map<String, Object> options);
    Document load(String content, Map<String, Object> options);

    void register_converter(RubyClass converter);
    void register_converter(RubyClass converter, String[] backends);
    RubyClass resolve_converter(String backend);
    RubyArray converters();
    void unregister_all_converters();

	String asciidoctorRuntimeEnvironmentVersion();
	
}
