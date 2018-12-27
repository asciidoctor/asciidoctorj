package org.asciidoctor;

import org.asciidoctor.api.DirectoryWalker;
import org.asciidoctor.api.Options;
import org.asciidoctor.api.OptionsBuilder;
import org.asciidoctor.api.ast.Document;
import org.asciidoctor.api.ast.DocumentHeader;
import org.asciidoctor.api.extension.ExtensionGroup;
import org.asciidoctor.api.ast.Document;
import org.asciidoctor.api.ast.DocumentHeader;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.api.extension.ExtensionGroup;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.RubyExtensionRegistry;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.asciidoctor.log.LogHandler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author lordofthejars
 */
public interface Asciidoctor {

    public static final String STRUCTURE_MAX_LEVEL = "STRUCTURE_MAX_LEVEL";

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content the AsciiDoc source as String.
     * @param options a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convert(String, Map)}.
     */
    @Deprecated
    String render(String content, Map<String, Object> options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content the AsciiDoc source as String.
     * @param options a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convert(String, Options)}.
     */
    @Deprecated
    String render(String content, Options options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content the AsciiDoc source as String.
     * @param options a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convert(String, OptionsBuilder)}.
     */
    @Deprecated
    String render(String content, OptionsBuilder options);

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     *
     * @param contentReader  where asciidoc content is read.
     * @param rendererWriter where rendered content is written. Writer is flushed, but not
     *                       closed.
     * @param options        a Hash of options to control processing (default: {}).
     * @throws IOException if an error occurs while writing rendered content, this
     *                     exception is thrown.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convert(Reader, Writer, Map)}.
     */
    @Deprecated
    void render(Reader contentReader, Writer rendererWriter, Map<String, Object> options) throws IOException;

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     *
     * @param contentReader  where asciidoc content is read.
     * @param rendererWriter where rendered content is written. Writer is flushed, but not
     *                       closed.
     * @param options        a Hash of options to control processing (default: {}).
     * @throws IOException if an error occurs while writing rendered content, this
     *                     exception is thrown.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convert(Reader, Writer, Map)}.
     */
    @Deprecated
    void render(Reader contentReader, Writer rendererWriter, Options options) throws IOException;

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     *
     * @param contentReader  where asciidoc content is read.
     * @param rendererWriter where rendered content is written. Writer is flushed, but not
     *                       closed.
     * @param options        a Hash of options to control processing (default: {}).
     * @throws IOException if an error occurs while writing rendered content, this
     *                     exception is thrown.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convert(Reader, Writer, OptionsBuilder)}.
     */
    @Deprecated
    void render(Reader contentReader, Writer rendererWriter, OptionsBuilder options) throws IOException;

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as File path.
     * <p>
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
     * @param file    an input Asciidoctor file.
     * @param options a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a file.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convertFile(File, Map)}.
     */
    @Deprecated
    String renderFile(File file, Map<String, Object> options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as File path.
     * <p>
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
     * @param file    an input Asciidoctor file.
     * @param options a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a file.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convertFile(File, Options)}.
     */
    @Deprecated
    String renderFile(File file, Options options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as File path.
     * <p>
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
     * @param file    an input Asciidoctor file.
     * @param options a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a file.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convertFile(File, OptionsBuilder)}.
     */
    @Deprecated
    String renderFile(File file, OptionsBuilder options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     *
     * @param directoryWalker strategy used to retrieve all files to be rendered.
     * @param options         a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written to a file.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convertDirectory(DirectoryWalker, Map)}.
     */
    @Deprecated
    String[] renderDirectory(DirectoryWalker directoryWalker, Map<String, Object> options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     *
     * @param directoryWalker strategy used to retrieve all files to be rendered.
     * @param options         a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written to a file.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convertDirectory(DirectoryWalker, Options)}.
     */
    @Deprecated
    String[] renderDirectory(DirectoryWalker directoryWalker, Options options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     *
     * @param directoryWalker strategy used to retrieve all files to be rendered.
     * @param options         a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written to a file.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convertDirectory(DirectoryWalker, OptionsBuilder)}.
     */
    @Deprecated
    String[] renderDirectory(DirectoryWalker directoryWalker, OptionsBuilder options);

    /**
     * Parses all files added inside a collection.
     *
     * @param asciidoctorFiles to be rendered.
     * @param options          a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written to a file.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convertFiles(Collection, Map)}.
     */
    @Deprecated
    String[] renderFiles(Collection<File> asciidoctorFiles, Map<String, Object> options);

    /**
     * Parses all files added inside a collection.
     *
     * @param asciidoctorFiles to be rendered.
     * @param options          a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written to a file.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convertFiles(Collection, Options)}.
     */
    @Deprecated
    String[] renderFiles(Collection<File> asciidoctorFiles, Options options);

    /**
     * Parses all files added inside a collection.
     *
     * @param asciidoctorFiles to be rendered.
     * @param options          a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written to a file.
     * @since 1.6.0
     * @deprecated Will be removed in the next major version (2.0.0). Please use {@link #convertFiles(Collection, OptionsBuilder)}.
     */
    @Deprecated
    String[] renderFiles(Collection<File> asciidoctorFiles, OptionsBuilder options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content the AsciiDoc source as String.
     * @param options a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    String convert(String content, Map<String, Object> options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content        the AsciiDoc source as String.
     * @param options        a Hash of options to control processing (default: {}).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convert(String, Map)} is the same.
     * @return the rendered output String is returned
     */
    <T> T convert(String content, Map<String, Object> options, Class<T> expectedResult);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content the AsciiDoc source as String.
     * @param options a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    String convert(String content, Options options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content        the AsciiDoc source as String.
     * @param options        a Hash of options to control processing (default: {}).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convert(String, Options)} is the same.
     * @return the rendered output String is returned
     */
    <T> T convert(String content, Options options, Class<T> expectedResult);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content the AsciiDoc source as String.
     * @param options a Hash of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    String convert(String content, OptionsBuilder options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content        the AsciiDoc source as String.
     * @param options        a Hash of options to control processing (default: {}).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convert(String, OptionsBuilder)} is the same.
     * @return the rendered output String is returned
     */
    <T> T convert(String content, OptionsBuilder options, Class<T> expectedResult);

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     *
     * @param contentReader  where asciidoc content is read.
     * @param rendererWriter where rendered content is written. Writer is flushed, but not
     *                       closed.
     * @param options        a Hash of options to control processing (default: {}).
     * @throws IOException if an error occurs while writing rendered content, this
     *                     exception is thrown.
     */
    void convert(Reader contentReader, Writer rendererWriter,
                 Map<String, Object> options) throws IOException;

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     *
     * @param contentReader  where asciidoc content is read.
     * @param rendererWriter where rendered content is written. Writer is flushed, but not
     *                       closed.
     * @param options        a Hash of options to control processing (default: {}).
     * @throws IOException if an error occurs while writing rendered content, this
     *                     exception is thrown.
     */
    void convert(Reader contentReader, Writer rendererWriter, Options options)
            throws IOException;

    /**
     * Parse the document read from reader, and rendered result is sent to
     * writer.
     *
     * @param contentReader  where asciidoc content is read.
     * @param rendererWriter where rendered content is written. Writer is flushed, but not
     *                       closed.
     * @param options        a Hash of options to control processing (default: {}).
     * @throws IOException if an error occurs while writing rendered content, this
     *                     exception is thrown.
     */
    void convert(Reader contentReader, Writer rendererWriter,
                 OptionsBuilder options) throws IOException;

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as File path.
     * <p>
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
     * @param filename an input Asciidoctor file.
     * @param options  a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     * file.
     */
    String convertFile(File filename, Map<String, Object> options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as File path.
     * <p>
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
     * @param filename       an input Asciidoctor file.
     * @param options        a Hash of options to control processing (default: {}).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convertFile(File, Map)} is the same.
     * @return returns nothing if the rendered output is written to a
     * file.
     */
    <T> T convertFile(File filename, Map<String, Object> options, Class<T> expectedResult);


    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as File path.
     * <p>
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
     * @param filename an input Asciidoctor file.
     * @param options  a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     * file.
     */
    String convertFile(File filename, Options options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as File path.
     * <p>
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
     * @param filename       an input Asciidoctor file.
     * @param options        a Hash of options to control processing (default: {}).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convertFile(File, Map)} is the same.
     * @return returns nothing if the rendered output is written to a
     * file.
     */
    <T> T convertFile(File filename, Options options, Class<T> expectedResult);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as File path.
     * <p>
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
     * @param filename an input Asciidoctor file.
     * @param options  a Hash of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     * file.
     */
    String convertFile(File filename, OptionsBuilder options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * render it to the specified backend format.
     * <p>
     * Accepts input as File path.
     * <p>
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
     * @param filename       an input Asciidoctor file.
     * @param options        a Hash of options to control processing (default: {}).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convertFile(File, Map)} is the same.
     * @return returns nothing if the rendered output is written to a
     * file.
     */
    <T> T convertFile(File filename, OptionsBuilder options, Class<T> expectedResult);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     *
     * @param directoryWalker strategy used to retrieve all files to be rendered.
     * @param options         a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    String[] convertDirectory(DirectoryWalker directoryWalker,
                              Map<String, Object> options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     *
     * @param directoryWalker strategy used to retrieve all files to be rendered.
     * @param options         a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    String[] convertDirectory(DirectoryWalker directoryWalker, Options options);

    /**
     * Parse all AsciiDoc files found using DirectoryWalker instance.
     *
     * @param directoryWalker strategy used to retrieve all files to be rendered.
     * @param options         a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    String[] convertDirectory(DirectoryWalker directoryWalker,
                              OptionsBuilder options);

    /**
     * Parses all files added inside a collection.
     *
     * @param asciidoctorFiles to be rendered.
     * @param options          a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    String[] convertFiles(Collection<File> asciidoctorFiles,
                          Map<String, Object> options);

    /**
     * Parses all files added inside a collection.
     *
     * @param asciidoctorFiles to be rendered.
     * @param options          a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    String[] convertFiles(Collection<File> asciidoctorFiles, Options options);

    /**
     * Parses all files added inside a collection.
     *
     * @param asciidoctorFiles to be rendered.
     * @param options          a Hash of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    String[] convertFiles(Collection<File> asciidoctorFiles,
                          OptionsBuilder options);

    /**
     * Reads only header parameters instead of all document.
     *
     * @param filename to read the attributes.
     * @return header.
     */
    DocumentHeader readDocumentHeader(File filename);

    /**
     * Reads only header parameters instead of all document.
     *
     * @param content where rendered content is written. Writer is flushed, but not
     *                closed.
     * @return header.
     */
    DocumentHeader readDocumentHeader(String content);

    /**
     * Reads only header parameters instead of all document.
     *
     * @param contentReader where asciidoc content is read.
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
     *
     * @return Extension Registry object.
     */
    JavaExtensionRegistry javaExtensionRegistry();

    /**
     * Creates an Ruby extension registry ready to be used for registering all processors
     *
     * @return Extension Registry object.
     */
    RubyExtensionRegistry rubyExtensionRegistry();

    /**
     * Creates a registry for registering converters.
     *
     * @return Converter Registry object.
     */
    JavaConverterRegistry javaConverterRegistry();

    /**
     * Creates an ExtensionGroup that can be used to register and unregister a group of extensions.
     *
     * @return
     */
    ExtensionGroup createGroup();

    /**
     * Creates an ExtensionGroup that can be used to register and unregister a group of extensions.
     *
     * @return
     */
    ExtensionGroup createGroup(String groupName);

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
     *
     * @return Version number.
     */
    String asciidoctorVersion();

    /**
     * Factory for creating a new instance of Asciidoctor interface.
     *
     * @author lordofthejars
     */
    public static final class Factory {

        private Factory() {
        }

        /**
         * Creates a new instance of Asciidoctor.
         *
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         * Ruby calls.
         */
        public static Asciidoctor create() {
            return JRubyAsciidoctor.create();
        }

        /**
         * Creates a new instance of Asciidoctor and sets GEM_PATH environment
         * variable to provided gemPath. This method is mostly used in OSGi
         * environments.
         *
         * @param gemPath where gems are located.
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         * Ruby calls.
         */
        public static Asciidoctor create(String gemPath) {
            return JRubyAsciidoctor.create(gemPath);
        }

        /**
         * Creates a new instance of Asciidoctor and sets loadPath to provided paths. This method is mostly used in OSGi
         * environments.
         *
         * @param loadPaths where Ruby libraries are located.
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         * Ruby calls.
         */
        public static Asciidoctor create(List<String> loadPaths) {
            return JRubyAsciidoctor.create(loadPaths);
        }

        /**
         * Creates a new instance of Asciidoctor and sets a specific classloader for the  JRuby runtime to use.
         * This method is for use in environments like OSGi.
         * To initialize Asciidoctor in OSGi create the Asciidoctor instance like this:
         *
         * <pre>
         * org.jruby.javasupport.JavaEmbedUtils.initialize(Arrays.asList("uri:classloader:/gems/asciidoctor-1.5.8/lib"));
         * Asciidoctor asciidoctor = Asciidoctor.Factory.create(this.getClass().getClassLoader()); <3>
         * </pre>
         *
         * @param classloader
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         *         Ruby calls.
         */
        public static Asciidoctor create(ClassLoader classloader) {
            return JRubyAsciidoctor.create(classloader);
        }

        /**
         * Creates a new instance of Asciidoctor and sets a specific classloader and gempath for the JRuby runtime to use.
         * This method is for use in environments like OSGi.
         * To initialize Asciidoctor in OSGi create the Asciidoctor instance like this:
         *
         * <pre>
         * org.jruby.javasupport.JavaEmbedUtils.initialize(Arrays.asList("uri:classloader:/gems/asciidoctor-1.5.8/lib"));
         * Asciidoctor asciidoctor = Asciidoctor.Factory.create(this.getClass().getClassLoader()); <3>
         * </pre>
         * 
         * @param classloader
         * @param gemPath
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         */
        public static Asciidoctor create(ClassLoader classloader, String gemPath) {
            return JRubyAsciidoctor.create(classloader, gemPath);
        }

        /**
         * Creates a new instance of Asciidoctor and sets loadPath to provided paths.
         * The gem path of the Ruby instance is set to the gemPath parameter.
         *
         * @param loadPaths where Ruby libraries are located.
         * @param gemPath   where gems are located.
         * @return Asciidoctor instance which uses JRuby to wraps Asciidoctor
         * Ruby calls.
         */
        public static Asciidoctor create(List<String> loadPaths, String gemPath) {
            return JRubyAsciidoctor.create(loadPaths, gemPath);
        }

    }

    /**
     * Loads AsciiDoc content and returns the Document object.
     *
     * @param content to be parsed.
     * @param options
     * @return Document of given content.
     */
    Document load(String content, Map<String, Object> options);

    /**
     * Loads AsciiDoc content from file and returns the Document object.
     *
     * @param file    to be loaded.
     * @param options
     * @return Document of given content.
     */
    Document loadFile(File file, Map<String, Object> options);

    void registerLogHandler(LogHandler logHandler);

    void unregisterLogHandler(LogHandler logHandler);

}
