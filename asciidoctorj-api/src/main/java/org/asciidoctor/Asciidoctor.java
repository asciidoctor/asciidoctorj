package org.asciidoctor;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.converter.JavaConverterRegistry;
import org.asciidoctor.extension.ExtensionGroup;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.RubyExtensionRegistry;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.syntaxhighlighter.SyntaxHighlighterRegistry;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The main application interface (API) for Asciidoctor.
 * This API provides methods to:
 * <ul>
 *   <li>parse (aka. load) AsciiDoc content,
 *   <li>convert it to various output formats,
 *   <li>register extensions, custom converter and syntax highlighter.
 * </ul>
 *
 * @author lordofthejars
 */
public interface Asciidoctor extends AutoCloseable {

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as String object.
     * @deprecated User {@link #convert(String, Options)} instead.
     * 
     * @param content the AsciiDoc source as String.
     * @param options a Map of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    @Deprecated
    String convert(String content, Map<String, Object> options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as String object.
     * @deprecated Use {@link #convert(String, Options, Class)} instead.
     * 
     * @param content        the AsciiDoc source as String.
     * @param options        a Map of options to control processing (default: {}).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convert(String, Map)} is the same.
     * @return the rendered output String is returned
     */
    @Deprecated
    <T> T convert(String content, Map<String, Object> options, Class<T> expectedResult);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content the AsciiDoc source as String.
     * @param options options to control processing (default: empty).
     * @return the rendered output String is returned
     */
    String convert(String content, Options options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as String object.
     *
     * @param content        the AsciiDoc source as String.
     * @param options        options to control processing (default: empty).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convert(String, Options)} is the same.
     * @return the rendered output String is returned
     */
    <T> T convert(String content, Options options, Class<T> expectedResult);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as String object.
     * @deprecated Use {@link #convert(String, Options)} instead.
     * 
     * @param content the AsciiDoc source as String.
     * @param options a Map of options to control processing (default: {}).
     * @return the rendered output String is returned
     */
    @Deprecated
    String convert(String content, OptionsBuilder options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as String object.
     * @deprecated Use {@link #convert(String, Options, Class)} instead.
     * 
     * @param content        the AsciiDoc source as String.
     * @param options        a Map of options to control processing (default: {}).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convert(String, OptionsBuilder)} is the same.
     * @return the rendered output String is returned
     */
    @Deprecated
    <T> T convert(String content, OptionsBuilder options, Class<T> expectedResult);

    /**
     * Parse the document read from reader sending the converted result to
     * writer.
     * @deprecated Use {@link #convert(Reader, Writer, Options)} instead.
     * 
     * @param contentReader  where asciidoc content is read.
     * @param rendererWriter where rendered content is written. Writer is flushed, but not
     *                       closed.
     * @param options        a Map of options to control processing (default: {}).
     * @throws IOException if an error occurs while writing rendered content, this
     *                     exception is thrown.
     */
    @Deprecated
    void convert(Reader contentReader, Writer rendererWriter,
                 Map<String, Object> options) throws IOException;

    /**
     * Parse the document read from reader sending the converted result to
     * writer.
     *
     * @param contentReader  where asciidoc content is read.
     * @param rendererWriter where rendered content is written. Writer is flushed, but not
     *                       closed.
     * @param options        options to control processing (default: empty).
     * @throws IOException if an error occurs while writing rendered content, this
     *                     exception is thrown.
     */
    void convert(Reader contentReader, Writer rendererWriter, Options options)
            throws IOException;

    /**
     * Parse the document read from reader sending the converted result to
     * writer.
     * @deprecated Use {@link #convert(Reader, Writer, Options)} instead.
     * 
     * @param contentReader  where asciidoc content is read.
     * @param rendererWriter where rendered content is written. Writer is flushed, but not
     *                       closed.
     * @param options        a Map of options to control processing (default: {}).
     * @throws IOException if an error occurs while writing rendered content, this
     *                     exception is thrown.
     */
    @Deprecated
    void convert(Reader contentReader, Writer rendererWriter,
                 OptionsBuilder options) throws IOException;

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as File.
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
     * @deprecated Use {@link #convertFile(File, Options)} instead.
     * 
     * @param file    an input Asciidoctor file.
     * @param options a Map of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     * file.
     */
    @Deprecated
    String convertFile(File file, Map<String, Object> options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as File.
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
     * @deprecated User {@link #convertFile(File, Options, Class)} instead.
     * 
     * @param file           an input Asciidoctor file.
     * @param options        a Map of options to control processing (default: {}).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convertFile(File, Map)} is the same.
     * @return returns nothing if the rendered output is written to a
     * file.
     */
    @Deprecated
    <T> T convertFile(File file, Map<String, Object> options, Class<T> expectedResult);


    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as File.
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
     * @param options options to control processing (default: empty).
     * @return returns nothing if the rendered output String is written to a
     * file.
     */
    String convertFile(File file, Options options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as File.
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
     * @param file           an input Asciidoctor file.
     * @param options        options to control processing (default: empty).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convertFile(File, Map)} is the same.
     * @return returns nothing if the rendered output is written to a
     * file.
     */
    <T> T convertFile(File file, Options options, Class<T> expectedResult);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as File.
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
     * @deprecated Use {@link #convertFile(File, Options)} instead.
     * 
     * @param file    an input Asciidoctor file.
     * @param options a Map of options to control processing (default: {}).
     * @return returns nothing if the rendered output String is written to a
     * file.
     */
    @Deprecated
    String convertFile(File file, OptionsBuilder options);

    /**
     * Parse the AsciiDoc source input into an Document {@link Document} and
     * convert it to the specified backend format.
     * <p>
     * Accepts input as File.
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
     * @deprecated User {@link #convertFile(File, Options, Class)} instead.
     * 
     * @param file           an input Asciidoctor file.
     * @param options        a Map of options to control processing (default: {}).
     * @param expectedResult the expected return type. Usually {@link String} for HTML based formats.
     *                       In this case {@link #convertFile(File, Map)} is the same.
     * @return returns nothing if the rendered output is written to a
     * file.
     */
    @Deprecated
    <T> T convertFile(File file, OptionsBuilder options, Class<T> expectedResult);

    /**
     * Convert all AsciiDoc files found in directoryWalker.
     * See {@code AsciiDocDirectoryWalker} for reference strategy.
     * @deprecated Use {@link #convertDirectory(Iterable, Options)} instead.
     * 
     * @param directoryWalker strategy used to retrieve all files to be rendered.
     * @param options         a Map of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    @Deprecated
    String[] convertDirectory(Iterable<File> directoryWalker, Map<String, Object> options);

    /**
     * Convert all AsciiDoc files found in directoryWalker.
     * See {@code AsciiDocDirectoryWalker} for reference strategy.
     *
     * @param directoryWalker strategy used to retrieve all files to be rendered.
     * @param options         options to control processing (default: empty).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    String[] convertDirectory(Iterable<File> directoryWalker, Options options);

    /**
     * Convert all AsciiDoc files found in directoryWalker.
     * See {@code AsciiDocDirectoryWalker} for reference strategy.
     * @deprecated Use {@link #convertDirectory(Iterable, Options)} instead.
     *     
     * @param directoryWalker strategy used to retrieve all files to be rendered.
     * @param options         a Map of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    @Deprecated
    String[] convertDirectory(Iterable<File> directoryWalker, OptionsBuilder options);

    /**
     * Convert all files from a collection.
     * @deprecated Use {@link #convertFiles(Collection, Options)} instead.
     * 
     * @param files   to be converted.
     * @param options a Map of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    @Deprecated
    String[] convertFiles(Collection<File> files, Map<String, Object> options);

    /**
     * Convert all files from a collection.
     *
     * @param asciidoctorFiles to be converted.
     * @param options          options to control processing (default: empty).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    String[] convertFiles(Collection<File> asciidoctorFiles, Options options);

    /**
     * Convert all files from a collection.
     * @deprecated Use {@link #convertFiles(Collection, Options)} instead. 
     * 
     * @param files   to be converted.
     * @param options a Map of options to control processing (default: {}).
     * @return returns an array of 0 positions if the rendered output is written
     * to a file.
     */
    @Deprecated
    String[] convertFiles(Collection<File> files, OptionsBuilder options);

    /**
     * Loads the given Ruby gem(s) by name.
     *
     * @param requiredLibraries
     */
    void requireLibrary(String... requiredLibraries);

    /**
     * Loads the given Ruby gem in requiredLibraries by name.
     *
     * @param requiredLibraries
     */
    void requireLibraries(Collection<String> requiredLibraries);

    /**
     * Reads only header parameters instead of all document.
     *
     * @deprecated Use {@link #loadFile(File, Map)} instead.
     *
     * @param file to read the attributes.
     * @return header.
     */
    @Deprecated
    DocumentHeader readDocumentHeader(File file);

    /**
     * Reads only header parameters instead of all document.
     *
     * @deprecated Use {@link #load(String, Map)} instead.
     *
     * @param content where converted content is written. Writer is flushed, but not
     *                closed.
     * @return header.
     */
    @Deprecated
    DocumentHeader readDocumentHeader(String content);

    /**
     * Reads only header parameters instead of all document.
     *
     * @deprecated Use {@link #loadFile(File, Map)} instead.
     *
     * @param contentReader where asciidoc content is read.
     * @return header.
     */
    @Deprecated
    DocumentHeader readDocumentHeader(Reader contentReader);

    /**
     * Creates an extension registry ready to be used for registering Java extensions.
     *
     * @return Extension Registry object.
     */
    JavaExtensionRegistry javaExtensionRegistry();

    /**
     * Creates an Ruby extension registry ready to be used for registering Ruby extension.
     *
     * @return Extension Registry object.
     */
    RubyExtensionRegistry rubyExtensionRegistry();

    /**
     * Creates a registry for registering Java converters.
     *
     * @return Converter Registry object.
     */
    JavaConverterRegistry javaConverterRegistry();

    /**
     * Creates a registry for registering Java syntax highlighter.
     *
     * <p>This API is experimental and might change in an incompatible way in a minor version update!</p>
     *
     * @return Converter Registry object.
     */
    SyntaxHighlighterRegistry syntaxHighlighterRegistry();

    /**
     * Creates an ExtensionGroup that can be used to register and unregister multiples extensions all at once.
     *
     * @return Extension Group instance.
     */
    ExtensionGroup createGroup();

    /**
     * Creates an ExtensionGroup that can be used to register and unregister multiples extensions all at once.
     *
     * @param groupName to assign to the ExtensionGroup.
     * @return Extension Group instance.
     */
    ExtensionGroup createGroup(String groupName);

    /**
     * Unregister all registered extensions.
     */
    void unregisterAllExtensions();

    /**
     * This method frees all resources consumed by AsciidoctorJ module.
     * Keep in mind that if this method is called, instance becomes unusable and you should create another instance.
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
     */
    final class Factory {

        private Factory() {
        }

        /**
         * Creates a new instance of Asciidoctor.
         *
         * @return Asciidoctor instance which uses JRuby to wrap Asciidoctor
         * Ruby calls.
         */
        public static Asciidoctor create() {
            ServiceLoader<Asciidoctor> asciidoctorImpls = ServiceLoader.load(Asciidoctor.class);
            Iterator<Asciidoctor> iterator = asciidoctorImpls.iterator();
            if (!iterator.hasNext()) {
                asciidoctorImpls = ServiceLoader.load(Asciidoctor.class, Factory.class.getClassLoader());
                iterator = asciidoctorImpls.iterator();
            }
            if (iterator.hasNext()) {
                Asciidoctor impl = iterator.next();
                List<Asciidoctor> remainingImpls = new ArrayList<>();
                while (iterator.hasNext()) {
                    remainingImpls.add(iterator.next());
                }
                if (!remainingImpls.isEmpty()) {
                    remainingImpls.add(0, impl);
                    String remainingImplNames = remainingImpls
                            .stream()
                            .map(asciidoctor -> asciidoctor.getClass().getName())
                            .collect(Collectors.joining(",", "[", "]"));
                    throw new RuntimeException(String.format("Found multiple Asciidoctor implementations in the classpath: %s", remainingImplNames));
                }
                return impl;
            } else {
                throw new RuntimeException("Unable to find an implementation of Asciidoctor in the classpath (using ServiceLoader)");
            }
        }
    }

    /**
     * Loads AsciiDoc content and returns the Document object.
     * @deprecated Use {@link #load(String, Options)} instead.
     * 
     * @param content to be parsed.
     * @param options a Map of options to control processing (default: {}).
     * @return Document of given content.
     */
    @Deprecated
    Document load(String content, Map<String, Object> options);

    /**
     * Loads AsciiDoc content and returns the Document object.
     *
     * @param content to be parsed.
     * @param options options to control processing (default: empty).
     * @return Document of given content.
     */
    Document load(String content, Options options);
    
    /**
     * Loads AsciiDoc content from file and returns the Document object.
     * @deprecated Use {@link #loadFile(File, Options)} instead.
     * 
     * @param file    to be parsed.
     * @param options a Map of options to control processing (default: {}).
     * @return Document of given content.
     */
    @Deprecated
    Document loadFile(File file, Map<String, Object> options);

    /**
     * Loads AsciiDoc content from file and returns the Document object.
     *
     * @param file    to be parsed.
     * @param options options to control processing (default: empty).
     * @return Document of given content.
     */
    Document loadFile(File file, Options options);
    
    /**
     * Register a {@link LogHandler} to capture Asciidoctor message records.
     *
     * @param logHandler handler instance.
     */
    void registerLogHandler(LogHandler logHandler);

    /**
     * Unregister a {@link LogHandler}.
     *
     * @param logHandler handler instance.
     */
    void unregisterLogHandler(LogHandler logHandler);

    default <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(getClass())) {
            return clazz.cast(this);
        }
        throw new IllegalArgumentException("Cannot unwrap to " + clazz.getName());
    }

    @Override
    default void close() {
        // no-op
    }
}
