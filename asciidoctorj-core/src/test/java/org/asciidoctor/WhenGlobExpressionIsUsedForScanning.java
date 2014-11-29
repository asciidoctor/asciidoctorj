package org.asciidoctor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.asciidoctor.util.ClasspathResources;
import org.junit.Rule;
import org.junit.Test;

public class WhenGlobExpressionIsUsedForScanning {

    @Rule
    public ClasspathResources classpath = new ClasspathResources();

    @Test
    public void all_files_with_given_extension_of_current_directory_should_be_returned() {
        
        File pathToWalk = classpath.getResource("src");
        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(pathToWalk.getPath(), "documents/*.adoc");
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
        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(pathToWalk.getPath(), "**/*.adoc");
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
        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker(pathToWalk.getPath(), "**/*.a");
        List<File> asciidocFiles = globDirectoryWalker.scan();
        
        assertThat(asciidocFiles, is(empty()));
    }
    
}
