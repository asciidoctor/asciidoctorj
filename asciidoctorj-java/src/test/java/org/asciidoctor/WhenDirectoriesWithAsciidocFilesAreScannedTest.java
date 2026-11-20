package org.asciidoctor;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.jruby.AsciiDocDirectoryWalker;
import org.asciidoctor.jruby.DirectoryWalker;
import org.asciidoctor.nativeimage.RegisterDirectory;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
@RegisterDirectory("src/documents/")
public class WhenDirectoriesWithAsciidocFilesAreScannedTest {

    @Test
    public void only_asciidoc_files_should_be_returned(@ClasspathResource("src/documents/") File documentsFolder) {

        DirectoryWalker abstractDirectoryWalker = new AsciiDocDirectoryWalker(documentsFolder.getPath());
        List<File> asciidocFiles = abstractDirectoryWalker.scan();

        // Convert to absolute paths, otherwise Hamcrest's matchers fails
        List<String> asciidocFilesPaths = new ArrayList<>();
        for (File f : asciidocFiles) {
            asciidocFilesPaths.add(f.getAbsolutePath());
        }

        assertThat(asciidocFilesPaths).hasSize(4);
        assertThat(asciidocFilesPaths).containsExactlyInAnyOrder(
                new File(documentsFolder, "sample.ad").getAbsolutePath(),
                new File(documentsFolder, "sample.adoc").getAbsolutePath(),
                new File(documentsFolder, "sample.asciidoc").getAbsolutePath(),
                new File(documentsFolder, "sample.asc").getAbsolutePath());

    }

    @Test
    public void empty_directory_should_return_no_documents(@TempDir File temporaryFolder) {
        DirectoryWalker abstractDirectoryWalker = new AsciiDocDirectoryWalker(temporaryFolder.getAbsolutePath());
        List<File> asciidocFiles = abstractDirectoryWalker.scan();

        assertThat(asciidocFiles).isEmpty();
    }

    @Test
    public void none_existing_directories_should_return_no_documents() {
        DirectoryWalker abstractDirectoryWalker = new AsciiDocDirectoryWalker("my_udirectory");
        List<File> asciidocFiles = abstractDirectoryWalker.scan();

        assertThat(asciidocFiles).isEmpty();
    }

}
