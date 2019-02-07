package org.asciidoctor;

import org.asciidoctor.jruby.DirectoryWalker;
import org.asciidoctor.jruby.GlobDirectoryWalker;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class WhenGlobExpressionIsUsedForScanning {

    @ArquillianResource
    private ClasspathResources classpath;

    @Test
    public void all_files_with_given_extension_of_current_directory_should_be_returned() {
        
        File pathToWalk = classpath.getResource("src");
        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(pathToWalk.getPath() + File.separator + "documents/*.adoc");
        List<File> asciidocFiles = globDirectoryWalker.scan();
        
        assertThat(
                asciidocFiles,
                containsInAnyOrder(
                        classpath.getResource("src/documents/_sample.adoc"), 
                        classpath.getResource("src/documents/sample.adoc")));
    }
    
    @Test
    public void all_files_with_given_extension_should_be_returned_recursively() {
        
        File pathToWalk = classpath.getResource("src");
        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(pathToWalk.getPath() + File.separator + "**/*.adoc");
        List<File> asciidocFiles = globDirectoryWalker.scan();
        
        assertThat(
                asciidocFiles,
                containsInAnyOrder(
                        classpath.getResource("src/documents/_sample.adoc"),
                        classpath.getResource("src/documents/sample.adoc")));
    }
    
    @Test
    public void no_should_be_returned_if_glob_expression_does_not_match() {
        
        File pathToWalk = classpath.getResource("src");
        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(pathToWalk.getPath() + File.separator + "**/*.a");
        List<File> asciidocFiles = globDirectoryWalker.scan();
        
        assertThat(asciidocFiles, is(empty()));
    }

    @Test
    public void should_not_fail_with_file_in_root_dir() {

        final String fileNameInRootDir = "/this_file_does_not_exist.adoc";

        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(fileNameInRootDir);

        List<File> asciidocFiles = globDirectoryWalker.scan();

        assertThat(asciidocFiles, contains(new File(fileNameInRootDir).getAbsoluteFile()));
    }

    @Test
    public void should_not_fail_with_file_in_current_working_dir() {

        final String fileNameInCurrentWorkingDir = "this_file_does_not_exist.adoc";

        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(fileNameInCurrentWorkingDir);

        List<File> asciidocFiles = globDirectoryWalker.scan();

        assertThat(asciidocFiles, contains(new File(fileNameInCurrentWorkingDir).getAbsoluteFile()));
    }

}
