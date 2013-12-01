package org.asciidoctor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class WhenGlobExpressionIsUsedForScanning {

    @Test
    public void all_files_with_given_extension_of_current_directory_should_be_returned() {
        
        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker("target/test-classes/src", "documents/*.adoc");
        List<File> asciidocFiles = globDirectoryWalker.scan();
        
        assertThat(
                asciidocFiles,
                containsInAnyOrder(new File("target/test-classes/src/documents/_sample.adoc"), new File(
                        "target/test-classes/src/documents/sample.adoc")));
    }
    
    @Test
    public void all_files_with_given_extension_should_be_returned_recursively() {
        
        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker("target/test-classes/src", "**/*.adoc");
        List<File> asciidocFiles = globDirectoryWalker.scan();
        
        assertThat(
                asciidocFiles,
                containsInAnyOrder(new File("target/test-classes/src/documents/_sample.adoc"), new File(
                        "target/test-classes/src/documents/sample.adoc")));
    }
    
    @Test
    public void no_should_be_returned_if_glob_expression_does_not_match() {
        
        DirectoryWalker globDirectoryWalker = new GlobDirectoryWalker("target/test-classes/src", "**/*.a");
        List<File> asciidocFiles = globDirectoryWalker.scan();
        
        assertThat(
                asciidocFiles,is(empty()));
    }
    
}
