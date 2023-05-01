package org.asciidoctor;

import org.asciidoctor.jruby.DirectoryWalker;
import org.asciidoctor.jruby.GlobDirectoryWalker;
import org.asciidoctor.test.ClasspathResource;
import org.asciidoctor.test.extension.AsciidoctorExtension;
import org.asciidoctor.test.extension.ClasspathExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({AsciidoctorExtension.class, ClasspathExtension.class})
public class WhenGlobExpressionIsUsedForScanning {

    @ClasspathResource("src")
    private File pathToWalk;

    @ClasspathResource("src/documents/_sample.adoc")
    private File hiddenSample;

    @ClasspathResource("src/documents/sample.adoc")
    private File sample;


    @Test
    public void all_files_with_given_extension_of_current_directory_should_be_returned() {

        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(pathToWalk.getPath() + File.separator + "documents/*.adoc");
        List<File> asciidocFiles = globDirectoryWalker.scan();

        assertThat(asciidocFiles)
                .containsExactlyInAnyOrder(hiddenSample, sample);
    }

    @Test
    public void all_files_with_given_extension_should_be_returned_recursively() {

        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(pathToWalk.getPath() + File.separator + "**/*.adoc");
        List<File> asciidocFiles = globDirectoryWalker.scan();

        assertThat(asciidocFiles)
                .containsExactlyInAnyOrder(hiddenSample, sample);
    }

    @Test
    public void no_should_be_returned_if_glob_expression_does_not_match() {

        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(pathToWalk.getPath() + File.separator + "**/*.a");
        List<File> asciidocFiles = globDirectoryWalker.scan();

        assertThat(asciidocFiles).isEmpty();
    }

    @Test
    public void should_not_fail_with_file_in_root_dir() {

        final String fileNameInRootDir = "/this_file_does_not_exist.adoc";

        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(fileNameInRootDir);
        List<File> asciidocFiles = globDirectoryWalker.scan();

        assertThat(asciidocFiles)
                .contains(new File(fileNameInRootDir).getAbsoluteFile());
    }

    @Test
    public void should_not_fail_with_file_in_current_working_dir() {

        final String fileNameInCurrentWorkingDir = "this_file_does_not_exist.adoc";

        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(fileNameInCurrentWorkingDir);
        List<File> asciidocFiles = globDirectoryWalker.scan();

        assertThat(asciidocFiles)
                .containsExactly(new File(fileNameInCurrentWorkingDir).getAbsoluteFile());
    }
}
