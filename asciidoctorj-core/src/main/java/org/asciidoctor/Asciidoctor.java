package org.asciidoctor;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.ast.StructuredDocument;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.RubyExtensionRegistry;
import org.asciidoctor.internal.JRubyAsciidoctor;

/**
 * 
 * 
 * @author lordofthejars
 * 
 */
public interface Asciidoctor {

	public static final String STRUCTURE_MAX_LEVEL = "STRUCTURE_MAX_LEVEL";
	
    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as String object.
     * 
     * 
     * @param content
     *            the AsciiDoc source as String.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    String render(String content, Map<String, Object> options);

    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as String object.
     * 
     * 
     * @param content
     *            the AsciiDoc source as String.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    String render(String content, Options options);

    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as String object.
     * 
     * 
     * @param content
     *            the AsciiDoc source as String.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    String render(String content, OptionsBuilder options);

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     * 
     * @param contentReader
     *            where asciidoc content is read.
     * @param rendererWriter
     *            where rendered content is written. Writer is flushed, but not
     *            closed.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @throws IOException
     *             if an error occurs while writing rendered content, this
     *             exception is thrown.
     */
    void render(Reader contentReader, Writer rendererWriter,
            Map<String, Object> options) throws IOException;

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     * 
     * @param contentReader
     *            where asciidoc content is read.
     * @param rendererWriter
     *            where rendered content is written. Writer is flushed, but not
     *            closed.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @throws IOException
     *             if an error occurs while writing rendered content, this
     *             exception is thrown.
     */
    void render(Reader contentReader, Writer rendererWriter, Options options)
            throws IOException;

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     * 
     * @param contentReader
     *            where asciidoc content is read.
     * @param rendererWriter
     *            where rendered content is written. Writer is flushed, but not
     *            closed.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @throws IOException
     *             if an error occurs while writing rendered content, this
     *             exception is thrown.
     */
    void render(Reader contentReader, Writer rendererWriter,
            OptionsBuilder options) throws IOException;

    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as File path.
     * 
     * If the :in_place option is true, and the input is a File, the output is
     * written to a file adjacent to the input file, having an extension that
     * corresponds to the backend format. Otherwise, if the :to_file option is
     * specified, the file is written to that file. If :to_file is not an
     * absolute path, it is resolved relative to :to_dir, if given, otherwise
     * the Document#base_dir. If the target directory does not exist, it will
     * not be created unless the :mkdirs option is set to true. If the file
     * cannot be written because the target directory does not exist, or because
     * it falls outside of the Document#base_dir in safe mode, an IOError is
     * raised.
     * 
     * @param filename
     *            an input Asciidoctor file.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     *         file.
     */
    String renderFile(File filename, Map<String, Object> options);

    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as File path.
     * 
     * If the :in_place option is true, and the input is a File, the output is
     * written to a file adjacent to the input file, having an extension that
     * corresponds to the backend format. Otherwise, if the :to_file option is
     * specified, the file is written to that file. If :to_file is not an
     * absolute path, it is resolved relative to :to_dir, if given, otherwise
     * the Document#base_dir. If the target directory does not exist, it will
     * not be created unless the :mkdirs option is set to true. If the file
     * cannot be written because the target directory does not exist, or because
     * it falls outside of the Document#base_dir in safe mode, an IOError is
     * raised.
     * 
     * @param filename
     *            an input Asciidoctor file.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     *         file.
     */
    String renderFile(File filename, Options options);

    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as File path.
     * 
     * If the :in_place option is true, and the input is a File, the output is
     * written to a file adjacent to the input file, having an extension that
     * corresponds to the backend format. Otherwise, if the :to_file option is
     * specified, the file is written to that file. If :to_file is not an
     * absolute path, it is resolved relative to :to_dir, if given, otherwise
     * the Document#base_dir. If the target directory does not exist, it will
     * not be created unless the :mkdirs option is set to true. If the file
     * cannot be written because the target directory does not exist, or because
     * it falls outside of the Document#base_dir in safe mode, an IOError is
     * raised.
     * 
     * @param filename
     *            an input Asciidoctor file.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     *         file.
     */
    String renderFile(File filename, OptionsBuilder options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     * 
     * @param directoryWalker
     *            strategy used to retrieve all files to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] renderDirectory(DirectoryWalker directoryWalker,
            Map<String, Object> options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     * 
     * @param directoryWalker
     *            strategy used to retrieve all files to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] renderDirectory(DirectoryWalker directoryWalker, Options options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     * 
     * @param directoryWalker
     *            strategy used to retrieve all files to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] renderDirectory(DirectoryWalker directoryWalker,
            OptionsBuilder options);

    /**
     * Parses all files added inside a collection.
     * 
     * @param asciidoctorFiles
     *            to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] renderFiles(Collection<File> asciidoctorFiles,
            Map<String, Object> options);

    /**
     * Parses all files added inside a collection.
     * 
     * @param asciidoctorFiles
     *            to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] renderFiles(Collection<File> asciidoctorFiles, Options options);

    /**
     * Parses all files added inside a collection.
     * 
     * @param asciidoctorFiles
     *            to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] renderFiles(Collection<File> asciidoctorFiles,
            OptionsBuilder options);
        
    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as String object.
     * 
     * 
     * @param content
     *            the AsciiDoc source as String.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    String convert(String content, Map<String, Object> options);

    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as String object.
     * 
     * 
     * @param content
     *            the AsciiDoc source as String.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    String convert(String content, Options options);

    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as String object.
     * 
     * 
     * @param content
     *            the AsciiDoc source as String.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    String convert(String content, OptionsBuilder options);

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     * 
     * @param contentReader
     *            where asciidoc content is read.
     * @param rendererWriter
     *            where rendered content is written. Writer is flushed, but not
     *            closed.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @throws IOException
     *             if an error occurs while writing rendered content, this
     *             exception is thrown.
     */
    void convert(Reader contentReader, Writer rendererWriter,
            Map<String, Object> options) throws IOException;

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     * 
     * @param contentReader
     *            where asciidoc content is read.
     * @param rendererWriter
     *            where rendered content is written. Writer is flushed, but not
     *            closed.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @throws IOException
     *             if an error occurs while writing rendered content, this
     *             exception is thrown.
     */
    void convert(Reader contentReader, Writer rendererWriter, Options options)
            throws IOException;

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     * 
     * @param contentReader
     *            where asciidoc content is read.
     * @param rendererWriter
     *            where rendered content is written. Writer is flushed, but not
     *            closed.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @throws IOException
     *             if an error occurs while writing rendered content, this
     *             exception is thrown.
     */
    void convert(Reader contentReader, Writer rendererWriter,
            OptionsBuilder options) throws IOException;

    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as File path.
     * 
     * If the :in_place option is true, and the input is a File, the output is
     * written to a file adjacent to the input file, having an extension that
     * corresponds to the backend format. Otherwise, if the :to_file option is
     * specified, the file is written to that file. If :to_file is not an
     * absolute path, it is resolved relative to :to_dir, if given, otherwise
     * the Document#base_dir. If the target directory does not exist, it will
     * not be created unless the :mkdirs option is set to true. If the file
     * cannot be written because the target directory does not exist, or because
     * it falls outside of the Document#base_dir in safe mode, an IOError is
     * raised.
     * 
     * @param filename
     *            an input Asciidoctor file.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     *         file.
     */
    String convertFile(File filename, Map<String, Object> options);

    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as File path.
     * 
     * If the :in_place option is true, and the input is a File, the output is
     * written to a file adjacent to the input file, having an extension that
     * corresponds to the backend format. Otherwise, if the :to_file option is
     * specified, the file is written to that file. If :to_file is not an
     * absolute path, it is resolved relative to :to_dir, if given, otherwise
     * the Document#base_dir. If the target directory does not exist, it will
     * not be created unless the :mkdirs option is set to true. If the file
     * cannot be written because the target directory does not exist, or because
     * it falls outside of the Document#base_dir in safe mode, an IOError is
     * raised.
     * 
     * @param filename
     *            an input Asciidoctor file.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     *         file.
     */
    String convertFile(File filename, Options options);

    /**
     * Parse the AsciiDoc source input into an Document {@link DocumentRuby} and
     * render it to the specified backend format.
     * 
     * Accepts input as File path.
     * 
     * If the :in_place option is true, and the input is a File, the output is
     * written to a file adjacent to the input file, having an extension that
     * corresponds to the backend format. Otherwise, if the :to_file option is
     * specified, the file is written to that file. If :to_file is not an
     * absolute path, it is resolved relative to :to_dir, if given, otherwise
     * the Document#base_dir. If the target directory does not exist, it will
     * not be created unless the :mkdirs option is set to true. If the file
     * cannot be written because the target directory does not exist, or because
     * it falls outside of the Document#base_dir in safe mode, an IOError is
     * raised.
     * 
     * @param filename
     *            an input Asciidoctor file.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     *         file.
     */
    String convertFile(File filename, OptionsBuilder options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     * 
     * @param directoryWalker
     *            strategy used to retrieve all files to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] convertDirectory(DirectoryWalker directoryWalker,
            Map<String, Object> options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     * 
     * @param directoryWalker
     *            strategy used to retrieve all files to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] convertDirectory(DirectoryWalker directoryWalker, Options options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     * 
     * @param directoryWalker
     *            strategy used to retrieve all files to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] convertDirectory(DirectoryWalker directoryWalker,
            OptionsBuilder options);

    /**
     * Parses all files added inside a collection.
     * 
     * @param asciidoctorFiles
     *            to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] convertFiles(Collection<File> asciidoctorFiles,
            Map<String, Object> options);

    /**
     * Parses all files added inside a collection.
     * 
     * @param asciidoctorFiles
     *            to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] convertFiles(Collection<File> asciidoctorFiles, Options options);

    /**
     * Parses all files added inside a collection.
     * 
     * @param asciidoctorFiles
     *            to be rendered.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     *         to a file.
     */
    String[] convertFiles(Collection<File> asciidoctorFiles,
            OptionsBuilder options);
    
    /**
     * Reads and creates structured document containing header and content chunks.
     * By default it dig only one level down but it can be tweak by setting STRUCTURE_MAX_LEVEL
     * option.
     * 
     * @param filename
     *            to read the attributes.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return structured document.
     */
    StructuredDocument readDocumentStructure(File filename,Map<String,Object> options);
    
    /**
     * Reads and creates structured document containing header and content chunks.
     * By default it dig only one level down but it can be tweak by setting STRUCTURE_MAX_LEVEL
     * option.
     * 
     * @param content
     *            where rendered content is written. Writer is flushed, but not
     *            closed.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return structured document.
     */
    StructuredDocument readDocumentStructure(String content,Map<String,Object> options);

    /**
     * Reads and creates structured document containing header and content chunks.
     * By default it dig only one level down but it can be tweak by setting STRUCTURE_MAX_LEVEL
     * option.
     * 
     * @param contentReader
     *            where asciidoc content is read.
     * @param options
     *            a Hash of options to control processing (default: {}).
     * @return structured document.
     */
    StructuredDocument readDocumentStructure(Reader contentReader,Map<String,Object> options);

    
    /**
     * Reads only header parameters instead of all document.
     * 
     * @param filename
     *            to read the attributes.
     * @return header.
     */
    DocumentHeader readDocumentHeader(File filename);

    /**
     * Reads only header parameters instead of all document.
     * 
     * @param content
     *            where rendered content is written. Writer is flushed, but not
     *            closed.
     * @return header.
     */
    DocumentHeader readDocumentHeader(String content);

    /**
     * Reads only header parameters instead of all document.
     * 
     * @param contentReader
     *            where asciidoc content is read.
     * @return header.
     */
    DocumentHeader readDocumentHeader(Reader contentReader);

    /**
     * Require the given Ruby libraries by name when rendering.
     *
     * @param requiredLibraries
     */
    void requireLibrary(String... requiredLibraries);

    /**
     * Require the given Ruby libraries by name when rendering.
     *
     * @param requiredLibraries
     */
    void requireLibraries(Collection<String> requiredLibraries);

    /**
     * Creates an extension registry ready to be used for registering all processors
     * @return Extension Registry object.
     */
    JavaExtensionRegistry javaExtensionRegistry();
    
    /**
     * Creates an Ruby extension registry ready to be used for registering all processors
     * @return Extension Registry object.
     */
    RubyExtensionRegistry rubyExtensionRegistry();

    /**
     * Creates a registry for registering converters.
     * @return Converter Registry object.
     */
    JavaConverterRegistry javaConverterRegistry();
    
    /**
     * Unregister all registered extensions.
     */
    void unregisterAllExtensions();
    
    /**
     * This method frees all resources consumed by asciidoctorJ module. Keep in mind that if this method is called, instance becomes unusable and you should create another instance.
     */
    void shutdown();
    
    /**
     * Method that gets the asciidoctor version which is being used.. 
     * @return Version number.
     */
    String asciidoctorVersion();
    
    /**
     * Factory for creating a new instance of Asciidoctor interface.
     * 
     * @author lordofthejars
     * 
     */
    public static final class Factory {

        private Factory() {}
        /**
         * Creates a new instance of Asciidoctor.
         * 
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         *         Ruby calls.
         */
        public static Asciidoctor create() {
            return JRubyAsciidoctor.create();
        }

        /**
         * Creates a new instance of Asciidoctor and sets GEM_PATH environment
         * variable to provided gemPath. This method is mostly used in OSGi
         * environments.
         * 
         * @param gemPath
         *            where gems are located.
         * 
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         *         Ruby calls.
         */
        public static Asciidoctor create(String gemPath) {
            return JRubyAsciidoctor.create(gemPath);
        }
        
        /**
         * Creates a new instance of Asciidoctor and sets loadPath to provided paths. This method is mostly used in OSGi
         * environments.
         * 
         * @param loadPaths
         *            where Ruby libraries are located.
         * 
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         *         Ruby calls.
         */
        public static Asciidoctor create(List<String> loadPaths) {
            return JRubyAsciidoctor.create(loadPaths);
        }

        /**
         * Creates a new instance of Asciidoctor and sets a specific classloader for the  JRuby runtime to use. This method is mostly
         * used in environments where different threads may have different classloaders, like build tools sbt or ANT.
         *
         * @param classloader
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         *         Ruby calls.
         * @deprecated Please use {@link #create()} and set the TCCL before or {@link #create(List)} passing the paths
         * that you would have used to create the ClassLoader.
         */
        public static Asciidoctor create(ClassLoader classloader) {
            return JRubyAsciidoctor.create(classloader);
        }

        /**
         * Cerates a new instance of Asciidoctor and sets a specific classloader and gempath for the JRuby runtime to use.
         * @param classloader
         * @param gemPath
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         * @deprecated Please use {@link #create(String)} and set the TCCL before or {@link #create(List, String)}
         * passing the paths that you would have used to create the ClassLoader.
         */
        public static Asciidoctor create(ClassLoader classloader, String gemPath) {
            return JRubyAsciidoctor.create(classloader, gemPath);
        }

        /**
         * Creates a new instance of Asciidoctor and sets loadPath to provided paths.
         * The gem path of the Ruby instance is set to the gemPath parameter.
         *
         * @param loadPaths
         *            where Ruby libraries are located.
         * @param gemPath
         *           where gems are located.
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         *         Ruby calls.
         */
        public static Asciidoctor create(List<String> loadPaths, String gemPath) {
            return JRubyAsciidoctor.create(loadPaths, gemPath);
        }

    }

    /**
     * Loads AsciiDoc content and returns the Document object.
     * @param content to be parsed.
     * @param options 
     * @return Document of given content.
     */
    Document load(String content, Map<String, Object> options);

    /**
     * Loads AsciiDoc content from file and returns the Document object.
     * @param file to be loaded.
     * @param options 
     * @return Document of given content.
     */
    Document loadFile(File file, Map<String, Object> options);


}
