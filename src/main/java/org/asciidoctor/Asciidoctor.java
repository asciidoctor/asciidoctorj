package org.asciidoctor;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.asciidoctor.internal.Document;
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
	 * Parse the AsciiDoc source input into an Document {@link Document} and render it to the specified backend format.
	 * 
	 * Accepts input as String object.
	 * 
	 * 
	 * @param content the AsciiDoc source as String.
	 * @param options a Hash of options to control processing (default: {}).
	 * @return the rendered output String is returned
	 */
	String render(String content, Options options);
	
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
	String render(String content, OptionsBuilder options);
	
	/**
	 * Parse the document read from reader, and rendered result is sent to writer.
	 * 
	 * @param contentReader where asciidoc content is read.
	 * @param rendererWriter where rendered content is written. Writer is flushed, but not closed.
	 * @param options a Hash of options to control processing (default: {}).
	 * @throws IOException if an error occurs while writing rendered content, this exception is thrown.
	 */
	void render(Reader contentReader, Writer rendererWriter, Map<String, Object> options) throws IOException;
	
	/**
	 * Parse the document read from reader, and rendered result is sent to writer.
	 * 
	 * @param contentReader where asciidoc content is read.
	 * @param rendererWriter where rendered content is written. Writer is flushed, but not closed.
	 * @param options a Hash of options to control processing (default: {}).
	 * @throws IOException if an error occurs while writing rendered content, this exception is thrown.
	 */
	void render(Reader contentReader, Writer rendererWriter, Options options) throws IOException;
	
	/**
	 * Parse the document read from reader, and rendered result is sent to writer.
	 * 
	 * @param contentReader where asciidoc content is read.
	 * @param rendererWriter where rendered content is written. Writer is flushed, but not closed.
	 * @param options a Hash of options to control processing (default: {}).
	 * @throws IOException if an error occurs while writing rendered content, this exception is thrown.
	 */
	void render(Reader contentReader, Writer rendererWriter, OptionsBuilder options) throws IOException;
	
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
	String renderFile(File filename, Map<String, Object> options);
	
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
	String renderFile(File filename, Options options);
	
	String renderFileExtension(File filename, String extensionName, Options options);
	
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
	String renderFile(File filename, OptionsBuilder options);
	
	/**
	 * Parse all AsciiDoc files found using DirectoryWalker instance.  
	 * @param directoryWalker strategy used to retrieve all files to be rendered.
	 * @param options a Hash of options to control processing (default: {}).
	 * @return returns an array of 0 positions if the rendered output is written to a file.
	 */
	String[] renderDirectory(DirectoryWalker directoryWalker, Map<String, Object> options);
	
	/**
	 * Parse all AsciiDoc files found using DirectoryWalker instance.  
	 * @param directoryWalker strategy used to retrieve all files to be rendered.
	 * @param options a Hash of options to control processing (default: {}).
	 * @return returns an array of 0 positions if the rendered output is written to a file.
	 */
	String[] renderDirectory(DirectoryWalker directoryWalker, Options options);
	
	/**
	 * Parse all AsciiDoc files found using DirectoryWalker instance. 
	 * @param directoryWalker strategy used to retrieve all files to be rendered.
	 * @param options a Hash of options to control processing (default: {}).
	 * @return returns an array of 0 positions if the rendered output is written to a file.
	 */
	String[] renderDirectory(DirectoryWalker directoryWalker, OptionsBuilder options);
	
	/**
	 * Parses all files added inside a collection.
	 * @param asciidoctorFiles to be rendered.
	 * @param options a Hash of options to control processing (default: {}).
	 * @return returns an array of 0 positions if the rendered output is written to a file.
	 */
	String[] renderFiles(Collection<File> asciidoctorFiles, Map<String, Object> options);
	
	/**
	 * Parses all files added inside a collection.
	 * @param asciidoctorFiles to be rendered.
	 * @param options a Hash of options to control processing (default: {}).
	 * @return returns an array of 0 positions if the rendered output is written to a file.
	 */
	String[] renderFiles(Collection<File> asciidoctorFiles, Options options);
	
	/**
	 * Parses all files added inside a collection.
	 * @param asciidoctorFiles to be rendered.
	 * @param options a Hash of options to control processing (default: {}).
	 * @return returns an array of 0 positions if the rendered output is written to a file.
	 */
	String[] renderFiles(Collection<File> asciidoctorFiles, OptionsBuilder options);
	
	/**
	 * Reads only header parameters instead of all document.
	 * @param filename to read the attributes.
	 * @return header.
	 */
	DocumentHeader readDocumentHeader(File filename);
	
	/**
	 * Reads only header parameters instead of all document.
	 * @param content where rendered content is written. Writer is flushed, but not closed.
	 * @return header.
	 */
	DocumentHeader readDocumentHeader(String content);
	
	/**
	 * Reads only header parameters instead of all document.
	 * @param contentReader where asciidoc content is read.
	 * @return header.
	 */
	DocumentHeader readDocumentHeader(Reader contentReader);
	
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
		
		/**
		 * Creates a new instance of Asciidoctor and sets GEM_PATH environment variable to provided gemPath.
		 * This method is mostly used in OSGi environments. 
		 * 
		 * @param gemPath where gems are located.
		 * 
		 * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor Ruby calls.
		 */
		public static Asciidoctor create(String gemPath) {
			return JRubyAsciidoctor.create(gemPath);
		}
	}

}
