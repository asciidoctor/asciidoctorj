package org.asciidoctor;

import java.util.Map;

import org.asciidoctor.internal.JRubyAsciidoctor;

/**
 * 
 * 
 * @author lordofthejars
 *
 */
public interface Asciidoctor {

	/**
	 * Parse the AsciiDoc source input into an Document {@link Document} and render it to the specified backend format.
	 * 
	 * Accepts input as String object.
	 * 
	 * 
	 * @param content the AsciiDoc source as String.
	 * @param options a Hash of options to control processing (default: {}).
	 * @return the rendered output String is returned
	 */
	String render(String content, Map<String, Object> options);
	
	/**
	 *  Parse the AsciiDoc source input into an Document {@link Document} and render it to the specified backend format.
	 *  
	 *  Accepts input as File path.
	 *  
	 * 	If the :in_place option is true, and the input is a File, the output is
	 *  written to a file adjacent to the input file, having an extension that
	 *  corresponds to the backend format. Otherwise, if the :to_file option is
	 *  specified, the file is written to that file. If :to_file is not an absolute
	 *  path, it is resolved relative to :to_dir, if given, otherwise the
	 *  Document#base_dir. If the target directory does not exist, it will not be
	 *  created unless the :mkdirs option is set to true. If the file cannot be
	 *  written because the target directory does not exist, or because it falls
	 *  outside of the Document#base_dir in safe mode, an IOError is raised.
	 * 
	 * @param filename an input Asciidoctor file.
	 * @param options a Hash of options to control processing (default: {}).
	 * @return returns nothing if the rendered output String is written to a file.
	 */
	String renderFile(String filename, Map<String, Object> options);
	
	/**
	 * Factory for creating a new instance of Asciidoctor interface.
	 * @author lordofthejars
	 *
	 */
	public static class Factory {
		
		/**
		 * Creates a new instance of Asciidoctor.
		 * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor Ruby calls.
		 */
		public static Asciidoctor create() {
			return JRubyAsciidoctor.create();
		}
	}
	
}
