package org.asciidoctor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.arquillian.api.Shared;
import org.asciidoctor.jruby.AsciiDocDirectoryWalker;
import org.asciidoctor.jruby.DirectoryWalker;
import org.asciidoctor.util.ClasspathResources;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WhenDirectoriesWithAsciidocFilesAreScanned {

    @ArquillianResource
    private ClasspathResources classpath;

	@ArquillianResource(Shared.class)
	public TemporaryFolder temporaryFolder;
	
	@Test
	public void only_asciidoc_files_should_be_returned() {
    
		DirectoryWalker abstractDirectoryWalker = new AsciiDocDirectoryWalker(classpath.getResource("src/documents").getPath());
		List<File> asciidocFiles = abstractDirectoryWalker.scan();

		// Converto to absolute paths, otherwise Hamcrest's matchers fails
		List<String> asciidocFilesPaths = new ArrayList<String>();
		for (File f: asciidocFiles) {
		    asciidocFilesPaths.add(f.getAbsolutePath());
		}
		
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
	public void empty_directory_should_return_no_documents() {
		
		DirectoryWalker abstractDirectoryWalker = new AsciiDocDirectoryWalker(temporaryFolder.getRoot().getAbsolutePath());
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
