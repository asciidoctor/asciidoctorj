package org.asciidoctor;

import org.asciidoctor.jruby.AsciiDocDirectoryWalker;
import org.asciidoctor.jruby.DirectoryWalker;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.asciidoctor.test.extension.ClasspathHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenDirectoriesWithAsciidocFilesAreScanned {

    @Test
    public void only_asciidoc_files_should_be_returned(@ClasspathResource("src/documents") File documentsFolder) {

        DirectoryWalker abstractDirectoryWalker = new AsciiDocDirectoryWalker(documentsFolder.getPath());
        List<File> asciidocFiles = abstractDirectoryWalker.scan();

        // Converto to absolute paths, otherwise Hamcrest's matchers fails
        List<String> asciidocFilesPaths = new ArrayList<>();
        for (File f : asciidocFiles) {
            asciidocFilesPaths.add(f.getAbsolutePath());
        }

        final ClasspathHelper classpath = new ClasspathHelper(this.getClass());
        assertThat(asciidocFilesPaths, hasSize(4));
        assertThat(
                asciidocFilesPaths,
                containsInAnyOrder(
                        classpath.getResource("src/documents/sample.ad").getAbsolutePath(),
                        classpath.getResource("src/documents/sample.adoc").getAbsolutePath(),
                        classpath.getResource("src/documents/sample.asciidoc").getAbsolutePath(),
                        classpath.getResource("src/documents/sample.asc").getAbsolutePath()));

    }

    @Test
    public void empty_directory_should_return_no_documents(@TempDir File temporaryFolder) {
        DirectoryWalker abstractDirectoryWalker = new AsciiDocDirectoryWalker(temporaryFolder.getAbsolutePath());
        List<File> asciidocFiles = abstractDirectoryWalker.scan();

        assertThat(asciidocFiles, is(empty()));
    }

    @Test
    public void none_existing_directories_should_return_no_documents() {
        DirectoryWalker abstractDirectoryWalker = new AsciiDocDirectoryWalker("my_udirectory");
        List<File> asciidocFiles = abstractDirectoryWalker.scan();

        assertThat(asciidocFiles, is(empty()));
    }

}
